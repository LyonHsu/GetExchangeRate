package google.videoplay.gethttpdata

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Html
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * https://fxcashflow.pixnet.net/blog/post/46547314
 * 美元:USD	    加幣:CAD 	歐元:EUR
 * 英鎊:GBP	    瑞士法郎:CHF	瑞典幣:SEK  	日圓:JPY
 * 新台幣:TWD    澳幣:AUD     港幣:HKD	紐幣:NZD
 * 新加坡幣:SGD	南非幣:ZAR	韓元:KRW     人民幣：CNY
 * 泰銖:THB      越南盾:VND  印尼盾:IDR
 * https://www.xe.com/zh-HK/currency/
 *
 * https://rate.bot.com.tw/xrt/flcsv/0/day
 */
class MainActivity : AppCompatActivity() {
    var TAG = "MainActivity"//::class.simpleName.toString()

    /**
     * https://www.findrate.tw/converter/(from)/(to)/(num)/
     */
    var fromC = "USD" //從這個國家的幣值
    var toC="TWD"   //到這個國家的幣值
    var httpUrl = "https://www.findrate.tw/converter/"+fromC+"/"+toC+"/1/" // 一元人民幣->新台幣

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

    fun postData(): String? {
        val url = URL(httpUrl)
        var htmlInf: String? = ""
        try {
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
//                urlConnection.setChunkedStreamingMode(0) //不知道資料長度時呼叫，避免緩存耗盡
                urlConnection.setRequestMethod("GET") //預設是GET 用POST要改
                urlConnection.setConnectTimeout(30 * 1000);
                urlConnection.setReadTimeout(5000);
//                urlConnection.setDoOutput(true) //default is false,有輸出時須為true
//                urlConnection.setDoInput(true) //default is true,有輸入時須為true
//                val outputData: String = keyAndValue(key, value) //key和value字串的串接是要丟到網頁上的資料 ex: "stkNo=2300"
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
        } catch (e: Exception) {
            return "Exception:" + e
        } catch (e: IOException) {
            return "Exception:" + e
        }
        return htmlInf
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
//            Log.d(TAG," start:"+start)
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
