package backcmprofile.cy.cm;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btBackUp;
    String LOGTAG = "cy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
        btBackUp = (Button) findViewById(R.id.back_button);
        btBackUp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int clickId = v.getId();
        switch (clickId) {
            case R.id.back_button:
                try {
                    backUpProfile();
                } catch (IOException e) {
                    Log.e(LOGTAG, e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    private void backUpProfile() throws IOException {
        Process process = null;
        DataOutputStream os = null;
        BufferedReader osReader = null;
        BufferedReader osErrorReader = null;

        process = Runtime.getRuntime().exec("su");
        //获得进程的输入输出流
        os = new DataOutputStream(process.getOutputStream());
        osReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        osErrorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));


        String cmd = "cat " + "/data/system/profiles.xml" + " > " + getSDPath() + "/profiles.xml";
        String cmd1 = "cp " + "/data/system/profiles.xml " + getSDPath() + "/profiles.xml";

        os.writeBytes(cmd1);
        os.writeBytes("\nexit\n");
        os.flush();

    }

    private String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    private String getRootPath() {
        File sdDir = null;

        sdDir = Environment.getRootDirectory();

        return sdDir.toString();
    }
}
