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
            RunCmd2("adb", "forward tcp:12580 tcp:10086");
            //RunCmd2("adb", "shell am broadcast -a NotifyServiceStart");
            InitSocket();
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

      
        /// <summary>
        /// 发送消息
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void button2_Click(object sender, EventArgs e)
        {
            clientSocket.Send(Encoding.UTF8.GetBytes(txtContext.Text));
        }

    }
}
