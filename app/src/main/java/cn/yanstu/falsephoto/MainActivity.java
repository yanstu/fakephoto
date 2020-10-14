package cn.yanstu.falsephoto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.yanstu.falsephoto.utils.MosaicTool;
import cn.yanstu.falsephoto.utils.WaterMaskTool;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_GALLERY = 0x10;// 图库选取图片标识请求码
    private static final int CROP_PHOTO = 0x12;// 裁剪图片标识请求码

    @BindView(R.id.iv_pic)
    ImageView iv_pic;// imageView控件

    private File imageFile = null;// 声明File对象
    private Uri imageUri = null;// 裁剪后的图片uri
    private final String PATH = "https://7.dusays.com/2020/10/13/8b6b8ae79c79a.png";
    private Bitmap icon;
    private Bitmap finalImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestStoragePermission();// 申请存储权限
        getIconBitmap();
        ButterKnife.bind(this);// 控件绑定
    }

    /**
     * 单击事件绑定
     */
    @OnClick({R.id.btn_gallery, R.id.btn_save})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.btn_gallery:// 图库选择
                gallery();
                break;
            case R.id.btn_save:
                saveImage();
                break;
        }
    }

    @SuppressLint("ShowToast")
    private void saveImage() {
        String savePath = Environment.getExternalStorageDirectory() + "/假闪照/";
        File savePath2 = new File(savePath);
        if (!savePath2.exists()) savePath2.mkdirs();
        if (finalImg == null) {
            Toast.makeText(this, "还未生成假闪照", Toast.LENGTH_LONG).show();
            return;
        }
        @SuppressLint("SdCardPath") File f = new File(savePath, System.currentTimeMillis() + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(f);
            finalImg.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "保存成功，存放到 /假闪照/ 文件夹", Toast.LENGTH_LONG).show();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图库选择图片
     */
    private void gallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // 以startActivityForResult的方式启动一个activity用来获取返回的结果
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    /**
     * 接收#startActivityForResult(Intent, int)调用的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {// 操作成功了
            switch (requestCode) {
                case REQUEST_CODE_GALLERY:// 图库选择图片
                    Uri uri = data.getData();// 获取图片的uri
                    Intent intent_gallery_crop = new Intent("com.android.camera.action.CROP");
                    intent_gallery_crop.setDataAndType(uri, "image/*");
                    // 创建文件保存裁剪的图片
                    createImageFile();
                    imageUri = Uri.fromFile(imageFile);
                    if (imageUri != null) {
                        intent_gallery_crop.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        intent_gallery_crop.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    }
                    startActivityForResult(intent_gallery_crop, CROP_PHOTO);
                    break;
                case CROP_PHOTO:// 裁剪图片
                    try {
                        if (imageUri != null) {
                            displayImage(imageUri);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    }

    /**
     * 申请存储权限
     */
    private void requestStoragePermission() {
        int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * 创建File保存图片
     */
    private void createImageFile() {
        String savePath = Environment.getExternalStorageDirectory() + "/假闪照/.nomedia/";
        File savePath2 = new File(savePath);
        if (!savePath2.exists()) savePath2.mkdirs();
        try {
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete();
            }
            // 新建文件
            imageFile = new File(savePath,
                    System.currentTimeMillis() + "temp.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片处理并显示
     */
    private void displayImage(Uri imageUri) {
        Bitmap mosaicImg = MosaicTool.BitmapMosaic(BitmapFactory.decodeFile(imageUri.getPath()), 135);
        Bitmap waterMaskImg = WaterMaskTool.adjustOpacity(WaterMaskTool.changeBitmapSize(icon, 1.5), 210);
        Bitmap result = WaterMaskTool.createWaterMaskCenter(mosaicImg, waterMaskImg);
        finalImg = result;
        iv_pic.setImageBitmap(result);
    }

    /**
     * 从网络获取到闪电图标
     */
    private void getIconBitmap() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL bitmapUrl = new URL(PATH);
                    connection = (HttpURLConnection) bitmapUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    //通过返回码判断网络是否请求成功
                    if (connection.getResponseCode() == 200) {
                        InputStream inputStream = connection.getInputStream();
                        Bitmap shareBitmap = BitmapFactory.decodeStream(inputStream);
                        Message message = wxHandler.obtainMessage();
                        message.what = 0;
                        message.obj = shareBitmap;
                        wxHandler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler wxHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    icon = (Bitmap) msg.obj;
                    break;
            }
        }
    };

}
