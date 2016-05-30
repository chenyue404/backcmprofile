package backcmprofile.cy.cm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button btBackUp, btRestore;
    private static final String LOGTAG = MainActivity.class.getSimpleName();

    private Process process;
    private DataOutputStream os;
    private BufferedReader osReader;
    private BufferedReader osErrorReader;

    private String FILENAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FILENAME = getString(R.string.profile_name);

        initUI();
    }

    private void initUI() {
        btBackUp = (Button) findViewById(R.id.backUp_button);
        btRestore = (Button) findViewById(R.id.restore_button);
        btBackUp.setOnClickListener(this);
        btRestore.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int clickId = v.getId();
        switch (clickId) {
            case R.id.backUp_button:
                try {
                    backUpProfile();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.restore_button:
                try {
                    restoreProfile();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void getRoot() throws IOException {
        process = Runtime.getRuntime().exec("su");
        os = new DataOutputStream(process.getOutputStream());
        osReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        osErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    }

    private void restoreProfile() throws IOException, InterruptedException {
        getRoot();
        String cmd = "cp " + getSDPath() + "/" + FILENAME + " /data/system/" + FILENAME;
        os.writeBytes(cmd);

        String result = exitProcess();
        if (result.equals("0")) {
            new AlertDialog.Builder(this).setMessage(R.string.restore_success)
                    .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                try {
                                    rebootPhone();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            dialog.cancel();
                        }
                    })
                    .setNeutralButton(R.string.delete_back_up, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                deleteProfile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton(R.string.restart_later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setCancelable(false)
                    .show();
            return;
        }

        if (!TextUtils.isEmpty(result)) {

            if (result.contains("No such file")) {
                result = getString(R.string.restore_read_file_failure);
            }

            new AlertDialog.Builder(this).setMessage(result).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }
    }

    private void deleteProfile() throws IOException, InterruptedException {
        getRoot();

        String cmd = "rm -f " + getSDPath() + "/" + getString(R.string.profile_name);
        os.writeBytes(cmd);

        String result = exitProcess();
        if (result.equals("0")) {
            new AlertDialog.Builder(this).setMessage(R.string.back_up_file_deleted).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
            return;
        }

        if (!TextUtils.isEmpty(result)) {

            if (result.contains("No such file")) {
                result = getString(R.string.back_up_read_file_failure);
            }

            new AlertDialog.Builder(this).setMessage(result).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }
    }


    private void rebootPhone() throws IOException, InterruptedException {
        getRoot();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String cmd = "reboot";
        os.writeBytes(cmd);
        String result = exitProcess();
    }

    private void backUpProfile() throws IOException, InterruptedException {
        getRoot();
        String cmd = "cp " + "/data/system/" + FILENAME + " " + getSDPath() + "/" + FILENAME;
        os.writeBytes(cmd);

        String result = exitProcess();

        if (result.equals("0")) {
            new AlertDialog.Builder(this).setMessage(R.string.back_up_success).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
            return;
        }

        if (!TextUtils.isEmpty(result)) {

            if (result.contains("No such file")) {
                result = getString(R.string.back_up_read_file_failure);
            }

            new AlertDialog.Builder(this).setMessage(result).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }

    }

    private String exitProcess() throws InterruptedException, IOException {
        os.writeBytes("\nexit\n");
        os.flush();

        int processResult;
        String shellMessage;
        String errorMessage;

        //获取命令执行信息
        shellMessage = readOSMessage(osReader);
        errorMessage = readOSMessage(osErrorReader);

        //获得退出状态
        processResult = process.waitFor();

        Log.e(LOGTAG, "processResult : " + processResult);
        Log.e(LOGTAG, "shellMessage : " + shellMessage);
        Log.e(LOGTAG, "errorMessage : " + errorMessage);

        if (processResult == 1 && TextUtils.isEmpty(errorMessage)) {
            new AlertDialog.Builder(this).setMessage(R.string.can_not_get_root).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
        }
        if (processResult == 0) {
            errorMessage = "0";
        }
        return errorMessage;
    }


    private String getSDPath() {
//        File sdDir = null;
//        boolean sdCardExist = Environment.getExternalStorageState()
//                .equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
//        if (sdCardExist) {
//            sdDir = Environment.getExternalStorageDirectory();//获取根目录
//        }
//        return sdDir.toString();
        return "/mnt/sdcard";
    }

    private String getRootPath() {
        File sdDir = null;
        sdDir = Environment.getRootDirectory();
        return sdDir.toString();
    }

    //读取执行命令后返回的信息
    private static String readOSMessage(BufferedReader messageReader) throws IOException {
        StringBuilder content = new StringBuilder();
        String lineString;
        while ((lineString = messageReader.readLine()) != null) {
            Log.e(LOGTAG, "lineString : " + lineString);
            content.append(lineString).append("\n");
        }
        return content.toString();
    }
}
