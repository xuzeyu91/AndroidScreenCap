using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace WindowClient
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
            System.Windows.Forms.Control.CheckForIllegalCrossThreadCalls = false;
            //RunCmd2("adb", "shell am broadcast -a NotifyServiceStop");
         
        }

        static bool RunCmd2(string cmdExe, string cmdStr)
        {
            bool result = false;
            try
            {
                using (Process myPro = new Process())
                {
                    myPro.StartInfo.FileName = "cmd.exe";
                    myPro.StartInfo.UseShellExecute = false;
                    myPro.StartInfo.RedirectStandardInput = true;
                    myPro.StartInfo.RedirectStandardOutput = true;
                    myPro.StartInfo.RedirectStandardError = true;
                    myPro.StartInfo.CreateNoWindow = true;
                    myPro.Start();
                    //如果调用程序路径中有空格时，cmd命令执行失败，可以用双引号括起来 ，在这里两个引号表示一个引号（转义）
                    string str = string.Format(@"""{0}"" {1} {2}", cmdExe, cmdStr, "&exit");
                    myPro.StandardInput.WriteLine(str);
                    myPro.StandardInput.AutoFlush = true;
                    myPro.WaitForExit();
                    result = true;
                }
            }
            catch
            {

            }
            return result;
        }

        Socket clientSocket;
        private static byte[] result = new byte[1024000];
        IPAddress ip;
        /// <summary>
        /// 初始化连接
        /// </summary>
        /// 
        public static Image Base64ToImage(string strbase64)
        {
            //string base64Str = "图片的BASE64字符串";
            byte[] bytes = System.Convert.FromBase64String(strbase64.Replace("data:image/png;base64,", ""));
            using (System.IO.MemoryStream ms = new System.IO.MemoryStream(bytes))
            {
                return System.Drawing.Image.FromStream(ms);
            }
        }

        private void InitSocket() {
            ip = IPAddress.Parse("127.0.0.1");
            clientSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            try
            {
                clientSocket.Connect(new IPEndPoint(ip, 12580)); //配置服务器IP与端口  
                bool flag = false;
                int len = 0;
                int temp = 0;
                string b = "";
                Thread receiveThread = new Thread(() =>
                {
                    while (true)
                    {
                        try
                        {
                            int receiveLength = clientSocket.Receive(result);
                            if (receiveLength > 0)
                            {
                                if (flag)
                                {
                                    if (temp == len)
                                    {
                                        flag = false;
                                        len = 0;
                                        temp = 0;
                                        b = "";
                                    }
                                    else {
                                        temp += receiveLength;
                                        b +=  Encoding.ASCII.GetString(result, 0, receiveLength);
                                        if (temp == len) {
                                            pictureBox1.Image =Base64ToImage(b);
                                            flag = false;
                                            len = 0;
                                            temp = 0;
                                            b = "";
                                        }
                                    }
                                }
                                string str = Encoding.UTF8.GetString(result, 0, receiveLength);
                                if (str.IndexOf("length:")>-1) {
                                    string s1 = str.Replace("length:", "");
                                    flag = true;
                                    len = int.Parse(s1);
                                }                             
                            }
                        }
                        catch (Exception ex)
                        {
                          
                        }
                    }
                });
                receiveThread.IsBackground = true;
                receiveThread.Start();
            }
            catch(Exception ex)
            {

            }
        }

        private void button3_Click(object sender, EventArgs e)
        {
            RunCmd2("adb", "forward tcp:12580 tcp:10086");
            //RunCmd2("adb", "shell am broadcast -a NotifyServiceStart");
            InitSocket();

        }
        /// <summary>
        /// 发送消息
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button2_Click(object sender, EventArgs e)
        {
            clientSocket.Send(Encoding.UTF8.GetBytes("start"));
        }

        private void Form1_MouseClick(object sender, MouseEventArgs e)
        {

        }

        string checkpic = "";
        private void pictureBox1_MouseClick(object sender, MouseEventArgs e)
        {
            int x = e.X;
            int y = e.Y;
            if (pic_logo1.Visible == false && pic_logo2.Visible == false)
            {
                pic_logo1.Location = new Point(x, y + 30);
                pic_logo1.Visible = true;
            }
            else if (pic_logo1.Visible == true && pic_logo2.Visible == false)
            {
                pic_logo2.Location = new Point(x, y + 30);
                pic_logo2.Visible = true;
                checkpic = "2";
            }
            else {
                if (checkpic == "1")
                {
                    pic_logo2.Location = new Point(x, y + 30);
                    checkpic = "2";

                }
                else {
                    pic_logo1.Location = new Point(x, y + 30);
                    checkpic = "1";
                }
            }
        }

        private void button1_Click(object sender, EventArgs e)
        {
            int x1, x2, y1, y2;
            x1 = pic_logo1.Location.X;
            x2 = pic_logo2.Location.X;
            y1 = pic_logo1.Location.X;
            y2 = pic_logo2.Location.Y;
            int MathX = System.Math.Abs(x1 - x2);
            int MathY = System.Math.Abs(y1 - y2);
            double Gen = Math.Round(Math.Sqrt(MathX * MathX + MathY * MathY), 2);
            double time;
            if (Gen < 200)
            {
                time = Math.Round(Gen * 2.4, 0);
            }
            else
            {
                time = Math.Round(Gen * 2.6, 0);

            }
               
           

            clientSocket.Send(Encoding.UTF8.GetBytes("tyt" + time.ToString()));
            pic_logo1.Visible = false;
            pic_logo2.Visible = false;
            label1.Text = "距离：" + Gen.ToString();
            label2.Text = "时间：" + time.ToString();
        }

        private void button4_Click(object sender, EventArgs e)
        {
            int x1, x2, y1, y2;
            x1 = pic_logo1.Location.X;
            x2 = pic_logo2.Location.X;
            y1 = pic_logo1.Location.X;
            y2 = pic_logo2.Location.Y;
            int MathX = System.Math.Abs(x1 - x2);
            int MathY = System.Math.Abs(y1 - y2);
            double Gen = Math.Round(Math.Sqrt(MathX * MathX + MathY * MathY), 2);
            double time;
            if (Gen > 300) {
                time = Math.Round(Gen * 2.6, 0);
            }
            else if (Gen < 250)
            {
                time = Math.Round(Gen * 3.3, 0);
            }
            else {
                time = Math.Round(Gen * 3, 0);
            }

            clientSocket.Send(Encoding.UTF8.GetBytes("tyt" + time.ToString()));
            pic_logo1.Visible = false;
            pic_logo2.Visible = false;
            label1.Text = "距离：" + Gen.ToString();
            label2.Text = "时间：" + time.ToString();
        }
    }
}
