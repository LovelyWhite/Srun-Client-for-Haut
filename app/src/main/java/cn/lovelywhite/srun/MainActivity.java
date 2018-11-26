package cn.lovelywhite.srun;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

class StreamTools {
    static String readString(InputStream in) throws Exception {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        int len=-1;
        byte[]buffer=new byte[1024];//1kb
        while ((len=in.read(buffer))!=-1) {
            baos.write(buffer,0,len);
        }
        in.close();
        return new String(baos.toByteArray());
    }
}
class Info
{
    static String[] info;
    static void setInfo(final TextView textArea)
    {
        new Thread()
        {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://172.16.154.130/cgi-bin/rad_user_info").openConnection();
                    conn.setReadTimeout(500);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setConnectTimeout(500);
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        InputStream in = conn.getInputStream();
                        String content = StreamTools.readString(in);
                        if(content.contains("not_online"))
                        {
                            info = null;
                            textArea.setText("当前在线信息：");
                        }
                        else
                        {
                            System.out.println("1");
                            info = content.split(",");
                            System.out.println("asd"+textArea);
                            write(textArea);
                        }
                    }
                }
                catch (java.net.SocketTimeoutException e)
                {
                    textArea.setText("未连接校园网");
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }
    @SuppressLint("SetTextI18n")
    static void write(final TextView textArea)
    {
        if(textArea!=null)
        {
            System.out.println("2");
            textArea.setText("当前在线信息：\n账号："+Info.getAccount()+"\n开始时间："+Info.getLoginTime());
        }
    }
    private static String getAccount()
    {
        if(info!=null&&info.length!=0)
            return info[0];
        else
            return null;
    }
    private static String getLoginTime()
    {
        if (info!=null&&info.length!=0)
        {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date(Long.valueOf(info[1]+"000")));
        }
        else
        {
            return null;
        }
    }
}
public class MainActivity extends AppCompatActivity {

    private EditText mAccount, mPassword;
    TextView textArea;
    Button mLogin = null;
    Button mLogout = null;
    Button mRefresh = null;

    public void showToast(final String content) {
        System.out.println(content);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (content.contains("login_ok")) {
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    mLogin.setEnabled(false);
                    Info.setInfo(textArea);
                } else if (content.contains("ip_already")) {
                    Toast.makeText(getApplicationContext(), "当前IP已有账号登录", Toast.LENGTH_SHORT).show();
                } else if (content.contains("Password is error") || content.contains("password_algo_error")) {
                    Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
                } else if (content.contains("logout_ok")) {
                    Toast.makeText(getApplicationContext(), "登出成功", Toast.LENGTH_SHORT).show();
                    Info.info = null;
                    textArea.setText("当前在线信息：");
                    mLogin.setEnabled(true);
                } else if (content.contains("User not found")) {
                    Toast.makeText(getApplicationContext(), "账号不存在", Toast.LENGTH_SHORT).show();
                } else if (content.contains("LOGOUT failed")) {
                    Toast.makeText(getApplicationContext(), "登出失败", Toast.LENGTH_SHORT).show();
                } else if (content.contains("x1")) {
                    Toast.makeText(getApplicationContext(), "认证服务器没有响应", Toast.LENGTH_SHORT).show();
                } else if (content.contains("x2")) {
                    Toast.makeText(getApplicationContext(), "未连接至校园网", Toast.LENGTH_SHORT).show();
                } else if (content.contains("Limit Users Err")) {
                    Toast.makeText(getApplicationContext(), "该账号已在其他地方登录", Toast.LENGTH_SHORT).show();
                } else if (content.contains("You are not online")) {
                    Toast.makeText(getApplicationContext(), "当前IP无账号登录", Toast.LENGTH_SHORT).show();
                    textArea.setText("当前在线信息：");
                    mLogin.setEnabled(true);
                } else if (content.contains("missing_required_parameters_error")) {
                    Toast.makeText(getApplicationContext(), "请输入用户名和密码", Toast.LENGTH_SHORT).show();
                } else if (content.contains("Status_Err")) {
                    Toast.makeText(getApplicationContext(), "状态错误(可能是欠费)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), content, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAccount = findViewById(R.id.account);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.login);
        mLogout = findViewById(R.id.logout);
        mRefresh = findViewById(R.id.refresh);
        textArea = findViewById(R.id.textArea);
        Info.setInfo(textArea);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Info.info != null) {
                    mLogin.setEnabled(false);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            Data data = new Data(mAccount.getText().toString(), mPassword.getText().toString(), "login", "2");
                            try {
                                String URL = data.get();
                                HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
                                conn.setReadTimeout(500);
                                conn.setRequestMethod("POST");
                                conn.setDoInput(true);
                                conn.setConnectTimeout(500);
                                int code;
                                try {
                                    code = conn.getResponseCode();
                                } catch (SocketTimeoutException e) {
                                    code = 1;
                                }
                                if (code == 200) {
                                    InputStream in = conn.getInputStream();
                                    String content = StreamTools.readString(in);
                                    showToast(content);
                                } else if (code == 0) {
                                    showToast("x1");
                                } else if (code == 1) {
                                    showToast("x2");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Info.info != null) {
                    new Thread() {
                        @Override
                        public void run() {
                            Data data = new Data(Info.info[0], "", "logout", "2");
                            try {
                                String URL = data.get();
                                HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
                                conn.setReadTimeout(1000);
                                conn.setRequestMethod("POST");
                                conn.setDoInput(true);
                                conn.setConnectTimeout(1000);
                                int code = conn.getResponseCode();
                                if (code == 200) {
                                    InputStream in = conn.getInputStream();
                                    String content = StreamTools.readString(in);
                                    showToast(content);
                                } else {
                                    showToast("x1");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else {
                    showToast("You are not online");
                }
            }
        });
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Info.setInfo(textArea);
                Toast.makeText(getApplicationContext(), "已刷新", Toast.LENGTH_SHORT).show();
            }
        });
        mLogin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "测试功能：免密码登陆正在测试中", Toast.LENGTH_SHORT).show();
                if (Info.info != null) {
                    mLogin.setEnabled(false);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            Data data = new Data("abcde", "111111", "login", "2");
                            try {
                                String URL = data.get();
                                HttpURLConnection conn = (HttpURLConnection) new URL(URL).openConnection();
                                conn.setReadTimeout(500);
                                conn.setRequestMethod("POST");
                                conn.setDoInput(true);
                                conn.setConnectTimeout(500);
                                int code;
                                try {
                                    code = conn.getResponseCode();
                                } catch (SocketTimeoutException e) {
                                    code = 1;
                                }
                                if (code == 200) {
                                    InputStream in = conn.getInputStream();
                                    String content = StreamTools.readString(in);
                                    showToast(content);
                                } else if (code == 0) {
                                    showToast("x1");
                                } else if (code == 1) {
                                    showToast("x2");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                }
                return false;
            }
        });

    }
}
class Data {
    private String URL;
    private String action;
    private String userName;
    private String passWord;
    private String type;
    private String n;
    private String ac_id;

    Data(String userName, String passWord, String action, String ac_id) {
        URL = "http://172.16.154.130:69/cgi-bin/srun_portal?";
        this.userName = "username=%7bSRUN3%7d%0d%0a" + userEncode(userName);
        this.passWord = "&password=" + passWord;
        this.ac_id = "&ac_id=" + ac_id;
        this.action = "&action=" + action;
        n = "&n=117";
        type = "&type=11";
    }

    String get() {
        URL += userName += passWord += action += type += n += ac_id;
        return URL;
    }

    private String userEncode(String userName) {
        char[] r = userName.toCharArray();
        for (int i = 0; i < r.length; i++)
            r[i] += 4;
        return new String(r);
    }
}

