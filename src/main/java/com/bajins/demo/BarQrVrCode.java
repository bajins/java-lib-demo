package com.bajins.demo;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Hashtable;

/**
 * 条形码、二维码、三维码
 * https://blog.csdn.net/Marksinoberg/article/details/122649896
 */
public class BarQrVrCode {
    //生成的二维码的路径
    private static final String QR_CODE_IMAGE_PATH = "F:\\MyQRCode.png";
    //二维码中的内容
    private static final String QR_CODE_TEXT = "这是二维码中的内容";
    //二维码图片的宽度
    private static int WIDTH = 300;
    //二维码图片的高度
    private static int HEIGHT = 300;

    private static void generateQRCodeImage() throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);//H最高容错等级
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8);

        BitMatrix bitMatrix = qrCodeWriter.encode(QR_CODE_TEXT, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

        Path path = FileSystems.getDefault().getPath(QR_CODE_IMAGE_PATH);

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    public static void main(String[] args) {
        try {
            generateQRCodeImage();
        } catch (WriterException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
