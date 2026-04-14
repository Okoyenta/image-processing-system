package com.imageprocessing.processing_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imageprocessing.processing_service.mapping.ImageUploadMapper;
import com.imageprocessing.processing_service.model.ImageUpload;
import com.imageprocessing.processing_service.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingServiceImpl extends ServiceImpl<ImageUploadMapper, ImageUpload> implements ImageProcessingService {

    @Value("${app.processing.output-directory}")
    private String outputDirectory;

    @Value("${app.processing.max-width}")
    private int maxWidth;

    private final ImageUploadMapper imageUploadMapper;




    /**
     * @param event
     * @throws Exception
     */
    @Override
    public void process(ImageUpload event) throws Exception {
        Long imageId = event.getId();

        // Step 1: Mark as PROCESSING
        setStatus(imageId, "PROCESSING", null);

        try {
            // Step 2: Read original image from disk
            File inputFile = new File(event.getFilePath());
            BufferedImage original = ImageIO.read(inputFile);

            if (original == null) {
                throw new IllegalArgumentException("Cannot read image file: " + event.getFilePath());
            }

            // Step 3: Resize if wider than maxWidth
            BufferedImage resized = resize(original);

            // Step 4: Apply diagonal bold watermark
            BufferedImage watermarked = applyWatermark(resized);

            // Step 5: Save processed image
            String outName = "processed-" + imageId + ".jpg";
            Path outPath = Path.of(outputDirectory, outName);
            Files.createDirectories(outPath.getParent());
            ImageIO.write(watermarked, "jpg", outPath.toFile());

            // Step 6: Update DB with COMPLETED status + dimensions + processed path
            imageUploadMapper.update(null, new LambdaUpdateWrapper<ImageUpload>()
                    .eq(ImageUpload::getId,            imageId)
                    .set(ImageUpload::getStatus,        "COMPLETED")
                    .set(ImageUpload::getProcessedPath, outPath.toString())
                    .set(ImageUpload::getWidth,         watermarked.getWidth())
                    .set(ImageUpload::getHeight,        watermarked.getHeight()));

            log.info("[Processing] Completed imageId={}, output={}", imageId, outPath);

        } catch (Exception e) {
            setStatus(imageId, "FAILED", e.getMessage());
            log.error("[Processing] Failed imageId={}, reason={}", imageId, e.getMessage());
            throw e;
        }
    }


    // -------------------------------------------------------------------------
    // Resize: maintain aspect ratio, scale down if wider than maxWidth
    // -------------------------------------------------------------------------
    private BufferedImage resize(BufferedImage src) {
        if (src.getWidth() <= maxWidth) return src;

        int newHeight = (int) ((double) src.getHeight() / src.getWidth() * maxWidth);
        BufferedImage out = new BufferedImage(maxWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, maxWidth, newHeight, null);
        g.dispose();

        return out;
    }

    // -------------------------------------------------------------------------
    // Watermark: bold "PROCESSED" text drawn diagonally across the image
    // -------------------------------------------------------------------------
    private BufferedImage applyWatermark(BufferedImage src) {
        int width  = src.getWidth();
        int height = src.getHeight();

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();

        // Draw original image first
        g.drawImage(src, 0, 0, null);

        // Rendering hints for smooth text
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        // Font: Bold, size proportional to image width
        int  fontSize = Math.max(40, width / 8);
        Font font     = new Font("Arial", Font.BOLD, fontSize);
        g.setFont(font);

        // Calculate diagonal angle based on image dimensions
        double angle = Math.atan2(height, width);

        // Measure text size
        FontRenderContext frc        = g.getFontRenderContext();
        GlyphVector glyphVector = font.createGlyphVector(frc, "PROCESSED");
        Rectangle        textBounds  = glyphVector.getPixelBounds(frc, 0, 0);
        int textWidth  = textBounds.width;
        int textHeight = textBounds.height;

        // Center the text on the image
        AffineTransform transform = new AffineTransform();
        transform.translate((double) width / 2, (double) height / 2);
        transform.rotate(-angle);
        transform.translate((double) -textWidth / 2, (double) textHeight / 2);
        g.setTransform(transform);

        // Draw shadow for depth
        g.setColor(new Color(0, 0, 0, 80));
        g.drawString("PROCESSED", 3, 3);

        // Draw semi-transparent red watermark text
        g.setColor(new Color(255, 0, 0, 120));
        g.drawString("PROCESSED", 0, 0);

        g.dispose();
        return out;
    }

    private void setStatus(Long imageId, String status, String error) {
        imageUploadMapper.update(null, new LambdaUpdateWrapper<ImageUpload>()
                .eq(ImageUpload::getId,            imageId)
                .set(ImageUpload::getStatus,       status)
                .set(ImageUpload::getErrorMessage, error));
    }
}
