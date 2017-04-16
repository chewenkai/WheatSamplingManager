package com.jrs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * 思普瑞特打印机连接封装类
 * <br>用法：
 * 	<br>SprtPrinter sprtPrinter = new SprtPrinter();
 *  <br>sprtPrinter.connect(SprtPrinter.T5);
 *  <br>sprtPrinter.send("Hello world");
 *  <br>sprtPrinter.close();
 *  <br>
 *  <br>具体指令请参照相应厂商相应型号打印机开发文档
 *  
 * @author LEO
 *
 */
public class SprtPrinter {
	public static final String TIII = "TIII";
	public static final String T5 = "T5";
	public static final String T8 = "T8";
	public static final String T9 = "T9";
	public static final String CBT = "CBT";
	private long sleepTime = 1L;
	
	private BluetoothSocket printerSocket = null;
	private BluetoothDevice printerDevice = null;
	private boolean connected = false;
	private String charsetName = "gbk";
	private OutputStream sendStream;
	private String printerName;
	private UUID applicationUUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");


	/**
	 * 连接打印机
	 * <br>连接打印机前请先确保打印机蓝牙已经配对成功，并记下打印机名
	 * @param printerName 打印机名，如"TIII"、"T5"
	 * @return 连接成功时，返回true
	 * @throws Exception 
	 * 
	 */
	public boolean connect(String printerName) throws Exception {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		//adapter.cancelDiscovery();
		Set<BluetoothDevice> devices = adapter.getBondedDevices();
		
		if (devices.size() > 0)	{
			for (Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext();){
				Object element = iterator.next();
				BluetoothDevice bluetoothDevice = (BluetoothDevice)element;
				if(bluetoothDevice.getName().startsWith(printerName)){
					printerDevice = bluetoothDevice; 
					
//					Method m = printerDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
//					printerSocket = (BluetoothSocket) m.invoke(printerDevice, 1);
//					printerSocket.connect();

					printerSocket = printerDevice
							.createRfcommSocketToServiceRecord(applicationUUID);
					adapter.cancelDiscovery();
					printerSocket.connect();

					sendStream = printerSocket.getOutputStream();
					
					connected = true;
					this.printerName = printerName;
					
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 设置打印指令，具体指令详见厂商相应型号打印机开发文档
	 * @param command 打印机指令
	 * @return 0、指令设置成功；<br>-1、打印机未连接，请先调用connect方法连接打印机；<br>-2、有连接异常，可能需要重新配对。
	 */
	public int setCommand(byte[] command){
		return send(command);
	}

	/**
	 * 设置打印机字符集，目前思普瑞特打印机支持字符集为"gbk"
	 * @param charsetName
	 */
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	/**
	 * 唤醒打印机
	 * 根据厂商手册，打印机在一定时间无动作时，会进入休眠，在使用用最好进行唤醒。
	 */
	public void wakeUp(){
		try {
			send(new byte[]{27, 64});
			send(new byte[]{0});
			Thread.sleep(20L);
		} catch (Exception e) {
			Log.e("BluetoothPrinter", "唤醒打印机出错", e);
		}
	}
	
	public void printCodeBar(String code, int width, int height){
//		send(new byte[]{29, 119, (byte)width});				//设置条形码横向尺寸
//		send(new byte[]{29, 72, 0});						//允许禁止打印HRI字符
//		send(new byte[]{29, 104, (byte)height});	        //条码高度
//		send(new byte[]{0X1D, 0X6B, 0X49, (byte)(code.length() + 2), 0X7B, 0X42});			
//		send(code);	
//		send(new byte[]{0});								//打印条码结束
//		send(new byte[]{27, 74, 20});						//下移20个点
		
		send(new byte[]{29, 119, (byte)width, 29, 72, 0, 29, 104, (byte)height, 0X1D, 0X6B, 0X49, (byte)(code.length() + 2), 0X7B, 0X42});				//设置条形码横向尺寸
		send(code);	
		send(new byte[]{0, 27, 74, 20});								//打印条码结束
	}
	
	/**
	 * 打印QRCode
	 * @param code
	 */
	public void printQRCode(String code){
		try{
			Hashtable hints = new Hashtable();//设置编码类型
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			//编码
			BitMatrix bitMatrix = new QRCodeWriter().encode(code, BarcodeFormat.QR_CODE, 30,  30, hints);
			
			int width = bitMatrix.getWidth();
			int height = bitMatrix.getHeight();
			int times = 4;//经验证二维码采用4像素打印一个点，比较合适
			
			//转成点图形式
			byte[][] bitImg = new byte[height * times][width * times];
			for(int h = 0; h < height; h++){
				for(int w = 0; w < width; w++){
					byte bitByte = (byte)(bitMatrix.get(w, h) ? 1 : 0);
					for(int z = 0; z < times; z++){
						for(int t = 0; t < times; t++){
							bitImg[h * times + z][w * times + t] = bitByte;
						}
					}
				}
			}
			printImg(bitImg);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void printImg(File codeFile){
		FileInputStream fis;
		try {
			fis = new FileInputStream(codeFile);
			Bitmap bmp = BitmapFactory.decodeStream(fis);
			printImg(bmp, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将图像转化成点图打印，可以根据需要进行按倍数等比例放大
	 * @param bmp
	 * @param times
	 */
	public void printImg(Bitmap bmp, int times){
		try {
			int width = bmp.getWidth();
			int height = bmp.getHeight();
			
			byte[][] bitImg = new byte[height * times][width * times];
			for(int h = 0; h < height; h++){
				for(int w = 0; w < width; w++){
					byte bitByte = printPixel(bmp.getPixel(w, h));
					for(int z = 0; z < times; z++){
						for(int t = 0; t < times; t++){
							bitImg[h * times + z][w * times + t] = bitByte;
						}
					}
				} 
			}
			
			printImg(bitImg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打印点图
	 * 按像素点进行打印，该点打印则为1，不打印则为0
	 * @param bitImg
	 * @return
	 */
	public int printImg(byte[][] bitImg){
		int result = -1;
		try{
			result = send(new byte[]{0x1B, 0x33, 0x00});
			for(int i = 0; i < bitImg.length; i+=8){
				byte[] row = new byte[bitImg[i].length];
				for(int j = 0; j < bitImg[i].length; j++){
					for(int k = 0; k < 8; k++){//每8行组一个字节
						if(k > 0) {
							row[j] = (byte)(row[j] << 1);
							row[j] = (byte)(row[j] & 0xFE);
						}
						if(i + k < bitImg[i].length ){
							row[j] = (byte)(row[j] | bitImg[i + k][j]);
						}
					}
				}
				byte[] printLength = new byte[2];
				printLength[0] = (byte)(bitImg[i].length & 0xFF);
				printLength[1] = (byte)(bitImg[i].length >> 8 & 0xFF);
				
				result = send(new byte[]{0x1B, 0x2A, 0x01, printLength[0], printLength[1]});
				result = send(row);
				result = send(new byte[]{0x0A, 0x0B});
			}
			result = send(new byte[]{0x1B, 0x33, 0x1e});
		}catch(Exception e){
			e.printStackTrace();
			result = -200;
		}
		return result;
	}
	
//	public void printImg(Bitmap bmp){
//		try {
//			int width = bmp.getWidth();
//			int height = bmp.getHeight();
//			
//			byte[][] bitImg = new byte[height][width];
//			for(int h = 0; h < height; h++){
//				for(int w = 0; w < width; w++){
//					bitImg[h][w] = printPixel(bmp.getPixel(w, h));
//				} 
//			}
//			
//			send(new byte[]{0x1B, 0x33, 0x00});
//			for(int i = 0; i < bitImg.length; i+=8){
//				byte[] row = new byte[bitImg[i].length];
//				for(int j = 0; j < bitImg[i].length; j++){
//					for(int k = 0; k < 8; k++){//每8行组一个字节
//						if(k > 0) {
//							row[j] = (byte)(row[j] << 1);
//							row[j] = (byte)(row[j] & 0xFE);
//						}
//						if(i + k < height){
//							row[j] = (byte)(row[j] | bitImg[i + k][j]);
//						}
//					}
//				}
//				byte[] printLength = new byte[2];
//				printLength[0] = (byte)(bitImg[i].length & 0xFF);
//				printLength[1] = (byte)(bitImg[i].length >> 8 & 0xFF);
//				
//				send(new byte[]{0x1B, 0x2A, 0x01, printLength[0], printLength[1]});
//				send(row);
//				send(new byte[]{0x0A, 0x0B});
//			}
//			send(new byte[]{0x1B, 0x33, 0x1e});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private byte consByte(Bitmap bi, int width, int x, int y){
//		byte b = 0x00;
//		for(int i = 0; i < 8; i++){
//			if(i > 0) {
//				b = (byte)(b << 1);
//				b = (byte)(b & 0xFE);
//			}
//			if(x + i < width){
//				b = (byte)(b | printPixel(bi.getPixel(x + i, y)));
//			}
//		}
//		return b;
//	}
	
	private byte printPixel(int pixel) {
	    int red = (pixel >> 16) & 0xff;
	    int green = (pixel >> 8) & 0xff;
	    int blue = (pixel) & 0xff;
	    
	    if(red + green + blue < 128){
	    	return 0x01;
	    }else{
	    	return 0x00;
	    }
	}
	
	/**
	 * 打印字符串内容，字符串格式由上一指令指定，关于字符串打印格式与型号密切相关，请查阅厂商提供的相应文档
	 * @param content 字符串
	 * @return 0、打印成功；<br>-1、打印机未连接，请先调用connect方法连接打印机；<br>-2、有连接异常，可能需要重新配对。。
	 */
	public int send(String content){
		try {
			return send(content.getBytes(charsetName));
		} catch (UnsupportedEncodingException e) {
			return -3;
		}
	}
	
	/**
	 * 连接状态
	 * @return true表示当前连接正常；<br>false表示未连接蓝牙打印机
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * 打印字符数组内容，字符数组格式由上一指令指定，关于字符数组打印格式与型号密切相关，请查阅厂商提供的相应文档
	 * @param content 字符数组
	 * @return 0、打印成功；<br>-1、打印机未连接，请先调用connect方法连接打印机；<br>-2、有连接异常。
	 */
	public int send(byte[] content){
		try{
			if(connected){
				sendStream.write(content);
				sendStream.flush();
				Thread.sleep(sleepTime);
			}else{
				return -1;
			}
		}catch (Exception e){
			close();
			Log.e("BluetoothPrinter", "打印机byte数组出错", e);
			return -2;
		}
		return 0;
	}

	/**
	 * 关闭蓝牙打印机连接
	 * @return 0、关闭成功；<br>-1、未关闭成功。
	 */
	public int close(){
		try{
			if(sendStream != null) sendStream.close();
			if(printerSocket != null) printerSocket.close();
			connected = false;
		}catch (Exception e){
			Log.e("BluetoothPrinter", "关闭打印机出错", e);
			return -1;
		}
		return 0;
	}

	/**
	 * 获取打印机名称
	 * @return 返回打印机名称
	 */
	public String getPrinterName() {
		return printerName;
	}
	
}
