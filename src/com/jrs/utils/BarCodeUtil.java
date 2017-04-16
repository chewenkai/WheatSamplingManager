package com.jrs.utils;

import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class BarCodeUtil {
	public static boolean[][] qrCode(String content) {
		try {
			Hashtable hints = new Hashtable();//设置编码类型
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			//编码
			BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 10,  10, hints);
			//输出到文件，也可以输出到流
			
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			
			boolean[][] bitImg = new boolean[height][width];
			
			for(int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					bitImg[y][x] = bitMatrix.get(x, y);
				}
			}
			return bitImg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static byte[][] encode(String content) {
		try {
			Hashtable hints = new Hashtable();//设置编码类型
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			//编码
			BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 50,  50, hints);
			//输出到文件，也可以输出到流
			
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			int r_width = (int)Math.ceil(width/8.0);
			
			byte[][] bitImg = new byte[height][r_width];
			
			for(int y = 0; y < height; y++){
				for(int x = 0; x < r_width; x++){
					byte b = 0x00;
					for(int z = 0; z < 8 && x * 8 + z < width; z++){
						b = (byte)(b << 1);
						if(bitMatrix.get(x * 8 + z, y)){
							b = (byte)(b | 0x01);
						}
					}
					bitImg[y][x] = b;
				}
			}
			return bitImg;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void decode(String fileName){
		try{
//			BufferedImage image = ImageIO.read(new File(fileName));//读取文件
//			LuminanceSource source = new BufferedImageLuminanceSource(image);
//			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
//			//解码
//			Result result = new MultiFormatReader().decode(bitmap);
//			String resultStr = result.getText();
//			System.out.println(resultStr);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
