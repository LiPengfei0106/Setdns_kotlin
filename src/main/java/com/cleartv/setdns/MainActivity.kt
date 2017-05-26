package com.cleartv.setdns

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PowerManager
import android.provider.Settings.Global
import android.provider.Settings.Secure
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import java.lang.ref.WeakReference
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


class MainActivity : Activity() {

    val TAG = "SetDNS"

    val manufacturer = android.os.Build.MANUFACTURER
    val AUTO_CONFIGNET = "manual" //"dhcp" "manual"
    var gateway = ""
    val DNS1 = "114.114.114.114"
    val DNS2 = "223.5.5.5"
    val localIPAddres: String
        get() {

            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                            return inetAddress.getHostAddress().toString()
                        }
                    }
                }
            } catch (e: SocketException) {

            }

            return "0.0.0.0"
        }

    val SET_CONFIGNET = true
    val SET_GATEWAY = true
    val SET_DNS = true

    val TYPE_GLOBEL = 1
    val TYPE_SECURE = 0

    var settingDbType = TYPE_SECURE
    var rebootTime = 5
    var timer: Timer = Timer()
    var timerTask: TimerTask = object : TimerTask(){
        override fun run() {
            mHandler?.sendEmptyMessage(0)
        }
    }
    var mHandler: SetDNSHandler? = null
    var infoTextView: TextView? = null

    class SetDNSHandler(val activity: WeakReference<MainActivity>) : Handler(){

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            val mainActivity: MainActivity = activity.get()!!
            mainActivity.infoTextView?.text = "DNS设置成功，系统将在" + mainActivity.rebootTime + "秒后重启"
            mainActivity.rebootTime--
            if (mainActivity.rebootTime<0){
                mainActivity.timerTask.cancel()
                mainActivity.timer.cancel()
                (mainActivity.getSystemService(Context.POWER_SERVICE) as PowerManager).reboot("Set DNS Success!")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        infoTextView = findViewById(R.id.info) as TextView?
        mHandler = SetDNSHandler(WeakReference<MainActivity>(this))
        if(manufacturer.toLowerCase().contains("skyworth"))
            settingDbType = TYPE_GLOBEL

        if((SET_CONFIGNET && setAutoConfigNet()) or (SET_GATEWAY && setIPRoute()) or (SET_DNS && setDNS())){
            timer.schedule(timerTask,0,1000)
        }else{
            finish()
        }

    }

    private fun setAutoConfigNet() : Boolean {
        when (settingDbType){
            TYPE_GLOBEL -> {
                if(AUTO_CONFIGNET != Global.getString(contentResolver,"ethernet_mode")){
                    if(AUTO_CONFIGNET == "manual"){
                        Log.i(TAG,"关闭自动配置网络")
                        Global.putString(contentResolver, "ethernet_ip", localIPAddres)
                    }else{
                        Log.i(TAG,"打开自动配置网络")
                    }
                    return Global.putString(contentResolver,"ethernet_mode",AUTO_CONFIGNET)
                }
            }
            TYPE_SECURE -> {
                if(AUTO_CONFIGNET != Secure.getString(contentResolver,"ethernet_mode")){
                    if(AUTO_CONFIGNET == "manual"){
                        Log.i(TAG,"关闭自动配置网络")
                        Secure.putString(contentResolver, "ethernet_ip", localIPAddres)
                    }else{
                        Log.i(TAG,"打开自动配置网络")
                    }
                    return Secure.putString(contentResolver,"ethernet_mode",AUTO_CONFIGNET)
                }
            }
        }
        return false
    }

    private fun setIPRoute(): Boolean{
        if (TextUtils.isEmpty(gateway)) {
            val ipAddress = Secure.getString(contentResolver, "ethernet_ip")
            gateway = ipAddress.substring(0, ipAddress.lastIndexOf(".") + 1) + "1"
        }
        when (settingDbType){
            TYPE_GLOBEL -> {
                if(gateway != Global.getString(contentResolver,"ethernet_iproute")){
                    Log.i(TAG, "ethernet_iproute设置为：$gateway")
                    return Global.putString(contentResolver,"ethernet_iproute",gateway)
                }
            }
            TYPE_SECURE -> {
                if(gateway != Secure.getString(contentResolver,"ethernet_iproute")){
                    Log.i(TAG, "ethernet_iproute设置为：$gateway")
                    return Secure.putString(contentResolver,"ethernet_iproute",gateway)
                }
            }
        }
        return false
    }

    private fun setDNS(): Boolean{
        var hasChange:Boolean = false
        when (settingDbType){
            TYPE_GLOBEL -> {
                if(!TextUtils.isEmpty(Global.getString(contentResolver,"ethernet_dns")) && DNS1 != Global.getString(contentResolver,"ethernet_dns")){
                    Log.i(TAG, "ethernet_dns设置为：" + DNS1)
                    if(Global.putString(contentResolver,"ethernet_dns",DNS1))
                        hasChange = true
                }
                if(!TextUtils.isEmpty(Global.getString(contentResolver,"ethernet_dns1")) && DNS1 != Global.getString(contentResolver,"ethernet_dns1")){
                    Log.i(TAG, "ethernet_dns1设置为：" + DNS1)
                    if(Global.putString(contentResolver,"ethernet_dns1",DNS1))
                        hasChange = true
                }
                if(!TextUtils.isEmpty(Global.getString(contentResolver,"ethernet_dns2")) && DNS2 != Global.getString(contentResolver,"ethernet_dns2")){
                    Log.i(TAG, "ethernet_dns2设置为：" + DNS2)
                    if(Global.putString(contentResolver,"ethernet_dns2",DNS2))
                        hasChange = true
                }
            }
            TYPE_SECURE -> {
                if(!TextUtils.isEmpty(Secure.getString(contentResolver,"ethernet_dns")) && DNS1 != Secure.getString(contentResolver,"ethernet_dns")){
                    Log.i(TAG, "ethernet_dns设置为：" + DNS1)
                    if(Secure.putString(contentResolver,"ethernet_dns",DNS1))
                        hasChange = true
                }
                if(!TextUtils.isEmpty(Secure.getString(contentResolver,"ethernet_dns1")) && DNS1 != Secure.getString(contentResolver,"ethernet_dns1")){
                    Log.i(TAG, "ethernet_dns1设置为：" + DNS1)
                    if(Secure.putString(contentResolver,"ethernet_dns1",DNS1))
                        hasChange = true
                }
                if(!TextUtils.isEmpty(Secure.getString(contentResolver,"ethernet_dns2")) && DNS2 != Secure.getString(contentResolver,"ethernet_dns2")){
                    Log.i(TAG, "ethernet_dns2设置为：" + DNS2)
                    if(Secure.putString(contentResolver,"ethernet_dns2",DNS2))
                        hasChange = true
                }
            }
        }
        return hasChange
    }


}
