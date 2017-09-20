package jp.ad.iij.nuse;

import android.graphics.Canvas;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.*;

import be.uclouvain.multipathcontrol.system.Cmd;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    static final String TEST_HOST = "202.214.86.51";
    static final String path = "/data/data/jp.ad.iij.nuse/cache/";
    static final String netperf_args = "-H " + TEST_HOST + " -l3 -c -C -D1 -T 1/1 -p 443 -- -P80";
    static final String curl_args = "http://multipath-tcp.org";
    static final String iperf3_args = "-c  " + TEST_HOST + " -l3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // menu/toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // button
        Button button = (Button)findViewById(R.id.button_start);
        button.setOnClickListener(onClick_button);
        // radio box
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.app_list);
        radioGroup.check(R.id.radio_button_netperf);
        radioGroup.setOnCheckedChangeListener(OnCheckedChangeListener);   

        EditText edit = (EditText)findViewById(R.id.args);
        edit.setText(netperf_args, TextView.BufferType.NORMAL);

        // assets extraction
        try {
            _extract_assets("liblkl-hijack-mptcp.so");
            _extract_assets("lkl-hijack-android.sh");
            _extract_assets("netperf");
            _extract_assets("curl");
            _extract_assets("iperf3");
            Cmd.getFirstLine("chmod +x " + path +"/netperf", true);
            Cmd.getFirstLine("chmod +x " + path +"/curl", true);
            Cmd.getFirstLine("chmod +x " + path +"/iperf3", true);
        }catch (Exception e) {
        }

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.result_texts);
        //  tv.setText(stringFromJNI());
    }

    public void _extract_assets(String name) throws IOException {
        OutputStream myOutput = new FileOutputStream(path + name);
        byte[] buffer = new byte[1024];
        int length;

        InputStream myInput = getAssets().open(name);

        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();
    }

    // Button handling
    private void onClickButtonStart() {

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.app_list);
        RadioButton rdo = (RadioButton)findViewById(radioGroup.getCheckedRadioButtonId());
        EditText edit = (EditText)findViewById(R.id.args);
        Spinner spinner = (Spinner)findViewById(R.id.spinner);

        String exec = rdo.getText().toString();
        String args = edit.getText().toString();
        String libname = (String)spinner.getSelectedItem();
        String cmdline = "LKL_HIJACK_LIBNAME=" + libname + " sh " + path + "lkl-hijack-android.sh " + exec + " " + args;

        Toast.makeText(MainActivity.this, exec + " started", Toast.LENGTH_SHORT).show();
        TextView tv = (TextView) findViewById(R.id.result_texts);
        String ret = Cmd.getAllLinesString(cmdline, true);
        tv.setText(ret);

    }

    private View.OnClickListener onClick_button = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button_start:
                    onClickButtonStart();
                    break;
            }
        }
    };

    // Menu handling
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_NIU) {
            Toast.makeText(MainActivity.this,"not implemented yet", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // radio group
    private RadioGroup.OnCheckedChangeListener OnCheckedChangeListener =  
        new RadioGroup.OnCheckedChangeListener(){  
            @Override  
            public void onCheckedChanged(RadioGroup group, int checkedId) {  
                RadioButton rdo = (RadioButton)findViewById(checkedId);
                EditText edit = (EditText)findViewById(R.id.args);

                switch (checkedId) {
                    case R.id.radio_button_netperf:
                        edit.setText(netperf_args, TextView.BufferType.NORMAL);
                        break;
                    case R.id.radio_button_curl:
                        edit.setText(curl_args, TextView.BufferType.NORMAL);
                        break;
                    case R.id.radio_button_iperf3:
                        edit.setText(iperf3_args, TextView.BufferType.NORMAL);
                        break;
                }
            }
        };

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
