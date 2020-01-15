# GetExchangeRate
使用台灣銀行的網頁來獲取各國幣值轉換 by Lyon on Kotlin

API:https://www.findrate.tw/converter/CNY/TWD/1/
    https://www.findrate.tw/converter/(從這個國家的幣值)/(到這個國家的幣值)/(轉換幣值數量)/
  
AndroidManifest.xml
<!-- 存取網路權限 -->

    <uses-permission android:name="android.permission.INTERNET" />
    
 MainActivity.kt
 
    class MainActivity : AppCompatActivity() { 
        var fromC = "CNY" //從這個國家的幣值
        var toC="TWD"   //到這個國家的幣值
        var httpUrl = "https://www.findrate.tw/converter/"+fromC+"/"+toC+"/1/" // 一元人民幣->新台幣
        val SHOW_RESPONSE = 0
        val SHOW_RESPONSE = 0

        lateinit var Text:TextView
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            Text= findViewById<TextView>(R.id.title)
            Thread(Runnable {
                var  s=postData()
                Log.d(TAG,"postData1:"+s)
                var d=DataTransform(s)
                Log.d(TAG,"postData DataTransform:"+d)
                var message= Message();
                message.what=SHOW_RESPONSE;
                //将服务器返回的结果存放到Message中
                message.obj=d.toString()
                handler.sendMessage(message);
            }).start()
        }
        
        var handler= Handler(){
            val b = when (it.what) {
                SHOW_RESPONSE -> {
                    Log.d(TAG,"postData2:"+it.obj)
                    Text.text= Html.fromHtml( it.obj.toString())
                    Log.d(TAG,"postData3:"+Text.text)

                    true
                }
                else -> {
                    false
                }
            }
            b
        }
        
        fun postData(): String? {
            val url = URL(httpUrl)
            var htmlInf: String? = ""
            try {
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                try {
                    urlConnection.setRequestMethod("GET") //GET/POST
                    urlConnection.setConnectTimeout(30 * 1000);
                    urlConnection.setReadTimeout(5000);
                    var inp: InputStream = urlConnection.getInputStream()
                    var reader= BufferedReader(InputStreamReader(inp));
                    var response=StringBuilder();
                    var line:String;

                    //https://www.jianshu.com/p/832b19b8a025 IO操作
                    var i=0;
                    while(true){
                        //当有内容时读取一行数据，否则退出循环
                        line = reader.readLine() ?: break
                        response.append(line);
                        Log.d(TAG,"line["+i+"]:"+line)
                        i++
                    }
                    htmlInf= response.toString()
                } finally {
                    urlConnection.disconnect() //斷開連接
                }
            } catch (e: IOException) {
                return "Exception:" + e
            }
            return htmlInf
        }
        /**
         * https://foolcodefun.github.io/blog/android/2017/09/22/Android-%E7%B6%B2%E9%A0%81%E8%B3%87%E6%96%99%E6%93%B7%E5%8F%96.html
         */
        fun DataTransform(htmlInf: String?):String{
            if(htmlInf==null)
                return ""
            var result:String ?= ""
            var regex = "table"
            //使用正規化 找到HTML <table>這個標記
            var patterns: Pattern =Pattern.compile(regex)
            var matcher :Matcher = patterns.matcher(htmlInf)
            val b = matcher.matches()
            var subs :String ?= ""
            var start = 0
            var end = 0
            while(true){
                //当有内容时读取一行数据，否则退出循环
                if(!matcher.find()){
                    Log.e(TAG,"沒有找到"+regex+"!!!")
                   break
                }
                start = matcher.start()
                subs = htmlInf.substring(end,start)
                Log.d(TAG," subs:"+subs)
                if(subs.contains(fromC+" =")){
                    break
                }
                end = matcher.end()
            }
            Log.d(TAG," ===============================================================")
            var sPl = subs!!.split("<td>")
            for( i in sPl){
                Log.d(TAG," sPl:"+i.replace("</td>",""))
            }
            result = sPl[2].replace("</td>","").replace("<tr>","").replace("</tr>","")
            return result
        }
        /*
                資料在table
                [0]                            <table width="100%" class="s" border="0"><tr ><td width="47%" class="f36"  ><span style="float:right; ">1.00 CNY </span></td><td width="6%" rowspan="2" class="f48"><center>=</center></td><td width="47%"  class="f36">4.2360 TWD&nbsp;</td></tr><tr><td  ><span style="float:right; ">人民幣(CNY)</span></td>
                [1]                                 <td>台幣(TWD)</td></tr><tr>
                [2]                                <td><span style="float:right; ">1 CNY = 4.236 TWD</span></td>
                [3]                                 <td>&nbsp;</td>
                [4]                                 <td>1 TWD = 0.2361 CNY</td></tr>
                                            </table>
         */
    }
        
        
        
