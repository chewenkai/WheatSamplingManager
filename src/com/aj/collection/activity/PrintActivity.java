package com.aj.collection.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aj.Constant;
import com.aj.collection.R;
import com.aj.collection.tools.T;
import com.aj.collection.tools.Util;
import com.aj.collection.ui.HeadControlPanel;
import com.aj.collection.ui.HeadControlPanel.LeftImageOnClick;
import com.aj.collection.ui.HeadControlPanel.rightFirstImageOnClick;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.jrs.utils.SprtPrinter;

import java.io.File;
import java.util.Hashtable;

public class PrintActivity extends AppCompatActivity {
    private int tryNum = 3;

    private boolean isCancle = false;

    private String strPrint;
    private String printer;    //打印机句柄
    private String num;    //条形码


    private TextView strPrintTV;
    private LinearLayout increaseFont, reduseFont;
    private LinearLayout increaseCopies, reduseCopies;
    private TextView showSize, showCopies;
    private ImageView codeBar;
    private ProgressDialog proDialog;

    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * 设置打印机字体
     */
    private byte fontsize = 0x11;
    SharedPreferences sp_fondSize;

    //设置打印机份数
    private int printCopies = 1;
    SharedPreferences sp_printCopies;

    private SprtPrinter sprtPrinter;

    AlertDialog alertDialog;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_layout);
        builder = new AlertDialog.Builder(PrintActivity.this);
        sprtPrinter = new SprtPrinter();
        try {
            init();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        //1、打开蓝牙需要先拿到蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //拿到之后需进行一个判断，是否拿到了呀
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "这个设备不支持蓝牙!", Toast.LENGTH_SHORT).show();
        }
        //2、点击打印按钮，先打开蓝牙
        //不通知用户直接打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            //Toast.makeText(this, "这个设备打不开蓝牙呀", Toast.LENGTH_SHORT).show();
        }

        if (((CollectionApplication) getApplication()).getSprtPrinter() != null) {
            sprtPrinter = ((CollectionApplication) getApplication()).getSprtPrinter();
        } else {
            ((CollectionApplication) getApplication()).setSprtPrinter(sprtPrinter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sheet_menu_print, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_bar_print:

                builder.setMessage("是否打印？");
                builder.setPositiveButton("是", onClickListener);
                builder.setNegativeButton("否", null);
                alertDialog = builder.create();
                alertDialog.show();
                break;
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void init() throws WriterException {
        strPrintTV = (TextView) findViewById(R.id.print_content);
        Intent i = getIntent();
        //strPrint = i.getStringExtra("sheetJsonStr");
        strPrint = i.getStringExtra("toPrint");
        num = i.getStringExtra("num");
        //strPrint = parseJsonStr(strPrint);
        if (strPrint == null || strPrint.equals("")) {
            strPrint = getResources().getString(R.string.nothing_to_do);
        }

        strPrintTV.setText(strPrint);
        codeBar = (ImageView) findViewById(R.id.code_bar);
        Bitmap bitmap = createQRImage(num, ScreenUtils.dip2px(150, getApplicationContext()), ScreenUtils.dip2px(150, getApplicationContext()));// newBarCode(num);
        codeBar.setImageBitmap(bitmap);

        //关于打印字体

        showSize = (TextView) findViewById(R.id.showsize);
        sp_fondSize = getSharedPreferences(getString(R.string.sp_fontsize), Activity.MODE_PRIVATE);
        fontsize = ((byte) sp_fondSize.getInt(getString(R.string.sp_fontsize), 0x11));
        showSize.setText("字体大小：" + String.valueOf(parseSize(fontsize)));
        strPrintTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10 + (int) fontsize);

        increaseFont = (LinearLayout) findViewById(R.id.mafont);
        increaseFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fontsize >= 0x55)//最大值
                    return;

                fontsize = (byte) (((int) fontsize) + 0x11);
                showSize.setText("字体大小：" + String.valueOf(parseSize(fontsize)));
                sp_fondSize.edit().putInt(getString(R.string.sp_fontsize), fontsize).commit();
                strPrintTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10 + (int) fontsize);
            }
        });

        reduseFont = (LinearLayout) findViewById(R.id.refont);
        reduseFont.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fontsize <= 0x00)//最小值
                    return;

                fontsize = (byte) (((int) fontsize) - 0x11);
                showSize.setText("字体大小：" + String.valueOf(parseSize(fontsize)));
                sp_fondSize.edit().putInt(getString(R.string.sp_fontsize), fontsize).commit();
                strPrintTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10 + (int) fontsize);
            }
        });

        //关于打印份数

        showCopies = (TextView) findViewById(R.id.showcopies);
        sp_printCopies = getSharedPreferences(getString(R.string.sp_copies), Activity.MODE_PRIVATE);
        printCopies = sp_printCopies.getInt(getString(R.string.sp_copies), 1);
        showCopies.setText("打印份数：" + printCopies);

        increaseCopies = (LinearLayout) findViewById(R.id.increase_copies);
        increaseCopies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (printCopies >= 50)//打印份数超过50退出
                    return;
                //份数加1
                printCopies++;
                showCopies.setText("打印份数：" + printCopies);
                sp_printCopies.edit().putInt(getString(R.string.sp_copies), printCopies).commit();
            }
        });

        reduseCopies = (LinearLayout) findViewById(R.id.reduce_copies);
        reduseCopies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (printCopies <= 1)//打印份数最少为1
                    return;

                //份数减一
                printCopies--;
                showCopies.setText("打印份数：" + printCopies);
                sp_printCopies.edit().putInt(getString(R.string.sp_copies), printCopies).commit();
            }
        });

    }

    public Bitmap createQRImage(String url, int QR_WIDTH, int QR_HEIGHT) {
        try {
            //判断URL合法性
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            //下面这里按照二维码的算法，逐个生成二维码的图片，
            //两个for循环是图片横列扫描的结果
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }
                }
            }
            //生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //显示到一个ImageView上面
//			sweepIV.setImageBitmap(bitmap);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成条形码图片
     *
     * @param content
     * @return
     * @throws WriterException
     */
    private Bitmap newBarCode(String content) throws WriterException {
        // 生成一维条码,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(content,
                BarcodeFormat.CODE_128, 500, 200);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;


    }

    private DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            if (strPrint.equals(getResources().getString(R.string.nothing_to_do))) {
                T.showShort(getApplicationContext(), "无打印内容");
                return;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                sprtPrinter.close();
                mBluetoothAdapter.enable();
            }
            isCancle = false;
            printer = SprtPrinter.CBT;
            final PrinterThread printerThread = new PrinterThread();
            proDialog = new ProgressDialog(PrintActivity.this);
            proDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            proDialog.setTitle("打印样标");
            proDialog.setMessage("正在打印，请稍候...");
            proDialog.setProgress(0);
            proDialog.setIndeterminate(true);
            proDialog.setCancelable(true);
            proDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    isCancle = true;
                    tryNum = 0;
                }
            });
            proDialog.show();
            printerThread.start();
        }
    };

    private class PrinterThread extends Thread {
        public void run() {
            try {
                Message msg = handler.obtainMessage(2, "正在打印...");
                handler.sendMessage(msg);

                msg = handler.obtainMessage(2, "连接打印机" + printer + "...");
                handler.sendMessage(msg);

                //sprtPrinter.wakeUp();
                if (!sprtPrinter.isConnected()) {
                    if (!sprtPrinter.connect(printer)) {

                        if (isCancle) {
                            if (proDialog.isShowing())
                                proDialog.dismiss();
                            return;
                        }

                        Message msg1 = handler.obtainMessage(Constant.SHOW_MSG, "打印失败,请检查是否与打印机配对并在合法距离内");
                        handler.sendMessage(msg1);
                        return;
                    }
                }

                msg = handler.obtainMessage(2, "打印中...");
                handler.sendMessage(msg);



                for (int i = 0; i < printCopies; i++) {

                    sprtPrinter.send(String.valueOf(i+1));
                    sprtPrinter.send("\n\n");

                    // 设置打印字体 ，-2表示打印失败，重新打印
                    if (-2 == sprtPrinter.send(new byte[]{0x1D, 0x21, fontsize})) {
                        PrinterThread printerThread = new PrinterThread();
                        printerThread.start();
                        T.showShort(getApplicationContext(), "连接丢失，正在重连...");
                        return;
                    }

                    /*
                 * 0~7 对应纵向 1~8 倍	0、16、32、48、64、80、96、112 对应横向 1~8倍
				 * 横向纵向相加得到倍数 n	0x1d,0x21,n
				 * */
                    if (-2 == sprtPrinter.send(strPrintTV.getText().toString())) {
                        PrinterThread printerThread = new PrinterThread();
                        printerThread.start();
                        T.showShort(getApplicationContext(), "连接丢失，正在重连...");
                        return;
                    }
                    sprtPrinter.send("\n\n");
                    sprtPrinter.send(new byte[]{0x1D, 0x21, 0x00});
                    sprtPrinter.send(Util.getCurrentTime("打印日期：yyyy-MM-dd HH:mm:ss \n "));
                    sprtPrinter.send("\n\n");
                    sprtPrinter.send("\n");
                    System.out.println(num);
                    sprtPrinter.printQRCode(num);
                    sprtPrinter.send("\n");
                    sprtPrinter.send("\n");
                    sprtPrinter.send("\n");
                    Thread.sleep(500L);
                }


                sprtPrinter.printImg(new File(Environment.getExternalStorageDirectory(), "4.png"));
                //sprtPrinter.close();
                msg = handler.obtainMessage(1, "打印完成");
                handler.sendMessage(msg);

            } catch (Exception e) {
                if (tryNum == 0) {
                    tryNum = 3;
                    e.printStackTrace();

                    //打印失败时重启一下蓝牙
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.disable();
                        mBluetoothAdapter.enable();
                    }

                    //通知用户打印失败
                    Message msg = handler.obtainMessage(Constant.SHOW_MSG, "打印失败,请检查是否与打印机配对并在合法距离内");
                    handler.sendMessage(msg);
                    return;
                }
                PrinterThread printerThread = new PrinterThread();
                printerThread.start();
                tryNum--;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(alertDialog!=null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(PrintActivity.this, "打印成功", Toast.LENGTH_LONG).show();
                proDialog.dismiss();
            } else if (msg.what == Constant.SHOW_MSG) {
                proDialog.dismiss();

                T.showShort(PrintActivity.this, (String) msg.obj);
            } else if (msg.what == Constant.SHOW_MSG_AND_OPEN_SETTING) {
                proDialog.dismiss();

                T.showShort(PrintActivity.this, (String) msg.obj);

                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));

            } else {
                proDialog.setMessage((String) msg.obj);
            }
        }
    };

    private int parseSize(byte size) {
        switch (size) {
            case 0:
                return 0;
            case 17:
                return 1;
            case 34:
                return 2;
            case 51:
                return 3;
            case 68:
                return 4;
            case 85:
                return 5;

            default:
                return 1;
        }
    }
}
