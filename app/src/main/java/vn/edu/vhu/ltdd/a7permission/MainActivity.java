package vn.edu.vhu.ltdd.a7permission;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A7_231A290080";
    private static final String CHANNEL_ID = "a7_channel";
    private static final int NOTI_ID = 1001;

    private ImageView imgAnh;
    private TextView tvTrangThai;
    private TextView tvKichThuoc;

    // Bộ xin quyền CAMERA
    private final ActivityResultLauncher<String> xinQuyenCamera =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    duocCap -> {
                        if (duocCap) {
                            moCamera();
                        } else if (shouldShowRequestPermissionRationale(
                                Manifest.permission.CAMERA)) {
                            hienGiaiThich();
                        } else {
                            hienMoCaiDat();
                        }

                        capNhatTrangThai();
                    });

    // Bộ xin quyền thông báo
    private final ActivityResultLauncher<String> xinQuyenThongBao =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    duocCap -> {
                        if (duocCap) {
                            guiThongBao();
                        } else {
                            Toast.makeText(
                                    this,
                                    R.string.noti_denied,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                        capNhatTrangThai();
                    });

    // Bộ chụp ảnh xem trước
    private final ActivityResultLauncher<Void> chupAnh =
            registerForActivityResult(
                    new ActivityResultContracts.TakePicturePreview(),
                    (Bitmap bitmap) -> {
                        if (bitmap != null) {
                            // Hiển thị ảnh vừa chụp
                            imgAnh.setImageBitmap(bitmap);

                            // Hiển thị kích thước ảnh thực tế
                            String kichThuoc = getString(
                                    R.string.status_photo,
                                    bitmap.getWidth(),
                                    bitmap.getHeight()
                            );

                            tvKichThuoc.setText(kichThuoc);
                        } else {
                            Toast.makeText(
                                    this,
                                    R.string.photo_cancelled,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            bars.left,
                            bars.top,
                            bars.right,
                            bars.bottom
                    );

                    return insets;
                });

        // Kết nối Java với giao diện XML
        imgAnh = findViewById(R.id.imgAnh);
        tvTrangThai = findViewById(R.id.tvTrangThai);
        tvKichThuoc = findViewById(R.id.tvKichThuoc);

        Button btnChupAnh = findViewById(R.id.btnChupAnh);
        Button btnThongBao = findViewById(R.id.btnThongBao);
        Button btnCaiDat = findViewById(R.id.btnCaiDat);

        // Tạo kênh thông báo
        taoKenhThongBao();

        // Xử lý sự kiện các nút
        btnChupAnh.setOnClickListener(v -> kiemTraRoiChup());
        btnThongBao.setOnClickListener(
                v -> kiemTraRoiGuiThongBao());
        btnCaiDat.setOnClickListener(
                v -> moCaiDatUngDung());
    }

    // Cập nhật trạng thái khi quay lại ứng dụng
    @Override
    protected void onResume() {
        super.onResume();
        capNhatTrangThai();
    }

    // Kiểm tra và xin quyền CAMERA
    private void kiemTraRoiChup() {
        int trangThai = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        );

        if (trangThai == PackageManager.PERMISSION_GRANTED) {
            // Nhánh 1: Đã có quyền
            moCamera();

        } else if (shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA)) {
            // Nhánh 2: Đã từ chối, cần giải thích
            hienGiaiThich();

        } else {
            // Nhánh 3: Lần đầu hoặc quyền đã bị chặn
            xinQuyenCamera.launch(
                    Manifest.permission.CAMERA);
        }
    }

    // Giải thích lý do cần quyền CAMERA
    private void hienGiaiThich() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.rationale_title)
                .setMessage(R.string.rationale_camera)
                .setPositiveButton(
                        R.string.agree,
                        (dialog, which) ->
                                xinQuyenCamera.launch(
                                        Manifest.permission.CAMERA)
                )
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Hướng dẫn mở Cài đặt khi quyền bị chặn
    private void hienMoCaiDat() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.blocked_title)
                .setMessage(R.string.blocked_camera)
                .setPositiveButton(
                        R.string.open_settings,
                        (dialog, which) -> moCaiDatUngDung()
                )
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Mở chức năng chụp ảnh xem trước
    private void moCamera() {
        chupAnh.launch(null);
    }

    // Mở trang Cài đặt của ứng dụng
    private void moCaiDatUngDung() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

        intent.setData(
                Uri.fromParts("package", getPackageName(), null));

        startActivity(intent);
    }

    // Kiểm tra quyền gửi thông báo
    private void kiemTraRoiGuiThongBao() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            guiThongBao();
            return;
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {
            guiThongBao();
        } else {
            xinQuyenThongBao.launch(
                    Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // Tạo kênh thông báo trên Android 8 trở lên
    private void taoKenhThongBao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel kenh = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            kenh.setDescription(
                    getString(R.string.channel_desc));

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(kenh);
        }
    }

    // Gửi thông báo
    @SuppressLint("MissingPermission")
    private void guiThongBao() {
        // Kiểm tra lại quyền trước khi gửi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            xinQuyenThongBao.launch(
                    Manifest.permission.POST_NOTIFICATIONS);
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info)
                        .setContentTitle(
                                getString(R.string.noti_title))
                        .setContentText(
                                getString(R.string.noti_text))
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(this)
                    .notify(NOTI_ID, builder.build());

        } catch (SecurityException e) {
            Log.w(TAG, "Chưa được cấp quyền gửi thông báo", e);

            Toast.makeText(
                    this,
                    R.string.noti_denied,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // Cập nhật trạng thái quyền trên giao diện
    private void capNhatTrangThai() {
        if (tvTrangThai == null) {
            return;
        }

        boolean camera = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;

        boolean thongBao =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                        || ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;

        String trangThai = getString(
                R.string.status_format,
                getString(
                        camera
                                ? R.string.granted
                                : R.string.denied),
                getString(
                        thongBao
                                ? R.string.granted
                                : R.string.denied)
        );

        tvTrangThai.setText(trangThai);

        Log.d(TAG, trangThai.replace("\n", " | "));
    }
}
