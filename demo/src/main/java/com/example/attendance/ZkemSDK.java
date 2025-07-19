package com.example.attendance;




import com.alibaba.fastjson.JSON;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.DispatchEvents;
import com.jacob.com.STA;
import com.jacob.com.Variant;

import java.util.*;

/**
 * 中控考勤机sdk函数调用类
 * @author wangchao
 *
 */
public class ZkemSDK {


	private static ActiveXComponent zkem = new ActiveXComponent("zkemkeeper.ZKEM.1");

	/**
	 * 链接考勤机
	 *
	 * @param address 考勤机地址
	 * @param port    端口号
	 * @return
	 */
	public boolean connect(String address, int port) {
		boolean result = zkem.invoke("Connect_NET", address, port).getBoolean();
		return result;
	}

	/**
	 * 断开考勤机链接
	 */
	public void disConnect() {
		zkem.invoke("Disconnect");
	}


	/**
	 * 读取考勤记录到pc缓存。配合getGeneralLogData使用
	 *
	 * @return
	 */
	public static boolean readGeneralLogData() {
		boolean result = zkem.invoke("ReadGeneralLogData", 4).getBoolean();
		return result;
	}

	/**
	 * 读取该时间之后的最新考勤数据。 配合getGeneralLogData使用。//网上说有这个方法，但是我用的开发文档没有这个方法，也调用不到，我在controller中处理获取当天数据
	 *
	 * @param lastest
	 * @return
	 */
	public static boolean readLastestLogData(Date lastest) {
		boolean result = zkem.invoke("ReadLastestLogData", 2018 - 07 - 24).getBoolean();
		return result;
	}

	/**
	 * 获取缓存中的考勤数据。配合readGeneralLogData / readLastestLogData使用。
	 *
	 * @return 返回的map中，包含以下键值：
	 * "EnrollNumber"   人员编号
	 * "Time"           考勤时间串，格式: yyyy-MM-dd HH:mm:ss
	 * "VerifyMode"     验证方式  1：指纹 2：面部识别
	 * "InOutMode"      考勤状态 0：上班 1：下班 2：外出 3：外出返回 4：加班签到 5：加班签退
	 * "Year"          考勤时间：年
	 * "Month"         考勤时间：月
	 * "Day"           考勤时间：日
	 * "Hour"            考勤时间：时
	 * "Minute"        考勤时间：分
	 * "Second"        考勤时间：秒
	 */
	public static List<Map<String, Object>> getGeneralLogData() {
		Variant dwMachineNumber = new Variant(1, true);//机器号

		Variant dwEnrollNumber = new Variant("", true);
		Variant dwVerifyMode = new Variant(0, true);
		Variant dwInOutMode = new Variant(0, true);
		Variant dwYear = new Variant(0, true);
		Variant dwMonth = new Variant(0, true);
		Variant dwDay = new Variant(0, true);
		Variant dwHour = new Variant(0, true);
		Variant dwMinute = new Variant(0, true);
		Variant dwSecond = new Variant(0, true);
		Variant dwWorkCode = new Variant(0, true);
		List<Map<String, Object>> strList = new ArrayList<Map<String, Object>>();
		boolean newresult = false;
		do {
			Variant vResult = Dispatch.call(zkem, "SSR_GetGeneralLogData", dwMachineNumber, dwEnrollNumber, dwVerifyMode, dwInOutMode, dwYear, dwMonth, dwDay, dwHour, dwMinute, dwSecond, dwWorkCode);
			newresult = vResult.getBoolean();
			if (true) {
				String enrollNumber = dwEnrollNumber.getStringRef();

				//如果没有编号，则跳过。
				// if (enrollNumber == null || enrollNumber.trim().length() == 0)
				// continue;
				String month = dwMonth.getIntRef() + "";
				String day = dwDay.getIntRef() + "";
				if (dwMonth.getIntRef() < 10) {
					month = "0" + dwMonth.getIntRef();
				}
				if (dwDay.getIntRef() < 10) {
					day = "0" + dwDay.getIntRef();
				}
				String validDate = dwYear.getIntRef() + "-" + month + "-" + day;
				//String currentDate = DateUtils.getCurrentTime("yyyy-MM-dd");
				if (true) {
					Map<String, Object> m = new HashMap<String, Object>();
					//Map<String, Object> user = getUserInfoByNumber(enrollNumber);
					m.put("EnrollNumber", enrollNumber);
					m.put("Time", dwYear.getIntRef() + "-" + dwMonth.getIntRef() + "-" + dwDay.getIntRef() + " " + dwHour.getIntRef() + ":" + dwMinute.getIntRef() + ":" + dwSecond.getIntRef());
					m.put("VerifyMode", dwVerifyMode.getIntRef());
					m.put("InOutMode", dwInOutMode.getIntRef());
					m.put("Year", dwYear.getIntRef());
					m.put("Month", dwMonth.getIntRef());
					m.put("Day", dwDay.getIntRef());
					m.put("Hour", dwHour.getIntRef());
					m.put("Minute", dwMinute.getIntRef());
					m.put("Second", dwSecond.getIntRef());
					strList.add(m);
				}
			}
		} while (newresult == true);
		return strList;
	}
	/**
	 * 获取用户信息
	 *
	 * @return 返回的Map中，包含以下键值:
	 * "EnrollNumber"  人员编号
	 * "Name"          人员姓名
	 * "Password"      人员密码
	 * "Privilege"     特权 0位普通 3特权
	 * "Enabled"       是否启用
	 */
	public static List<UserInfo> getUserInfo() {
		List<UserInfo> userInfoList = new LinkedList<>();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		//将用户数据读入缓存中。
		boolean result = zkem.invoke("ReadAllUserID", 1).getBoolean();

		Variant v0 = new Variant(1);
		Variant sdwEnrollNumber = new Variant("", true);
		Variant sName = new Variant("", true);
		Variant sPassword = new Variant("", true);
		Variant iPrivilege = new Variant(0, true);
		Variant bEnabled = new Variant(false, true);

		while (result) {
			//从缓存中读取一条条的用户数据
			result = zkem.invoke("SSR_GetAllUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();

			//如果没有编号，跳过。
			String enrollNumber = sdwEnrollNumber.getStringRef();
			if (enrollNumber == null || enrollNumber.trim().length() == 0)
				continue;

			//由于名字后面会产生乱码，所以这里采用了截取字符串的办法把后面的乱码去掉了，以后有待考察更好的办法。
			//只支持2位、3位、4位长度的中文名字。
			String name = sName.getStringRef();
			int index = name.indexOf("\0");
			String newStr = "";
			if (index > -1) {
				name = name.substring(0, index);
			}
			if (sName.getStringRef().length() > 4) {
				name = sName.getStringRef().substring(0, 4);
			}
			//如果没有名字，跳过。
			if (name.trim().length() == 0)
				continue;
			UserInfo userInfo = new UserInfo();
			userInfo.setEnrollNumber(enrollNumber);
			userInfo.setName(name);
			userInfo.setPassword(sPassword.getStringRef());
			userInfo.setPrivilege(iPrivilege.getIntRef());
			userInfo.setEnabled((Boolean) bEnabled.getBooleanRef());
			userInfoList.add(userInfo);
		}
		return userInfoList;
	}


	/**
	 * 设置用户信息
	 *
	 * @param number
	 * @param name
	 * @param password
	 * @param isPrivilege 0為普通用戶,3為管理員;
	 * @param enabled     是否啟用
	 * @return
	 */
	public static boolean setUserInfo(String number, String name, String password, int isPrivilege, boolean enabled) {
		Variant v0 = new Variant(1);
		Variant sdwEnrollNumber = new Variant(number, true);
		Variant sName = new Variant(name, true);
		Variant sPassword = new Variant(password, true);
		Variant iPrivilege = new Variant(isPrivilege, true);
		Variant bEnabled = new Variant(enabled, true);

		boolean result = zkem.invoke("SSR_SetUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();
		return result;
	}

	/**
	 * 获取用户信息
	 *
	 * @param number 考勤号码
	 * @return
	 */
	public static Map<String, Object> getUserInfoByNumber(String number) {
		Variant v0 = new Variant(1);
		Variant sdwEnrollNumber = new Variant(number, true);
		Variant sName = new Variant("", true);
		Variant sPassword = new Variant("", true);
		Variant iPrivilege = new Variant(0, true);
		Variant bEnabled = new Variant(false, true);
		boolean result = zkem.invoke("SSR_GetUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();
		if (result) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("EnrollNumber", number);
			m.put("Name", sName.getStringRef());
			m.put("Password", sPassword.getStringRef());
			m.put("Privilege", iPrivilege.getIntRef());
			m.put("Enabled", bEnabled.getBooleanRef());
			return m;
		}
		return null;
	}
	/**
	 * 获取用户信息
	 *
	 * @param number 考勤号码
	 * @return
	 */
	public static String getUserInfoByName(String number) {
		Variant v0 = new Variant(1);
		Variant sdwEnrollNumber = new Variant(number, true);
		Variant sName = new Variant("", true);
		Variant sPassword = new Variant("", true);
		Variant iPrivilege = new Variant(0, true);
		Variant bEnabled = new Variant(false, true);
		boolean result = zkem.invoke("SSR_GetUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();

		System.out.println(sName+"-"+sPassword+"-"+iPrivilege+"-"+bEnabled);
		return sName.getStringRef();
	}
	public static UserInfo getUserInfoByNames(UserInfo userinfo) {
		Variant v0 = new Variant(1);
		Variant sdwEnrollNumber = new Variant(userinfo.getEnrollNumber(), true);
		Variant sName = new Variant("", true);
		Variant sPassword = new Variant("", true);
		Variant iPrivilege = new Variant(0, true);
		Variant bEnabled = new Variant(false, true);
		boolean result = zkem.invoke("SSR_GetUserInfo", v0, sdwEnrollNumber, sName, sPassword, iPrivilege, bEnabled).getBoolean();
		userinfo.setEnrollNumber(String.valueOf(sdwEnrollNumber));
		userinfo.setName(String.valueOf(sName));
		userinfo.setPassword(String.valueOf(sPassword));
		return userinfo;
	}

	//获取指纹
	public static Variant GetUserTmpStr(String i) {
		Variant v0 = new Variant(1);
		byte temp = 0;

		Variant b = new Variant(i, true);
		Variant c = new Variant(1, true);

		Variant d = new Variant("", true);
		Variant e = new Variant(0, true);

/*    Variant dwMachineNumber = new Variant(0, true);//机器号
      Variant dwEnrollNumber = new Variant(0, true);//用户号
      Variant dwFingerIndex = new Variant(0, true);//指纹索引号
      Variant TmpData = new Variant(0, true);//该参数返回指纹模板数据
      Variant TmpLength = new Variant(0, true);//该参数返回指纹模板长度*/
		boolean result = zkem.invoke("SSR_GetUserTmpStr",v0, b, c, d,e).getBoolean();
		System.out.println("1:"+d.toString());
		System.out.println("2:"+result);
		return d;

	}


//上传指纹
	public static void SetUserTmpStr(String x,Variant d) {
		Variant v0 = new Variant(1);
		Variant a = new Variant(x, true);
		Variant b = new Variant(2, true);

		Variant c = d;
/*    Variant dwMachineNumber = new Variant(0, true);//机器号
      Variant dwEnrollNumber = new Variant(0, true);//用户号
      Variant dwFingerIndex = new Variant(0, true);//指纹索引号
      Variant TmpData = new Variant(0, true);//该参数返回指纹模板数据
      Variant TmpLength = new Variant(0, true);//该参数返回指纹模板长度*/

		boolean result = zkem.invoke("SSR_SetUserTmpStr", v0, a, b, c).getBoolean();
		System.out.println("3:"+c.toString());
		System.out.println("4:"+result);

	}



//删除
	public static void ReadAllTemplate() {
		Variant v0 = new Variant(1);


   // Variant dwMachineNumber = new Variant(0, true);//机器号
      Variant dwEnrollNumber = new Variant("1", true);//用户号
      Variant dwFingerIndex = new Variant(6, true);//指纹索引号
    //  Variant TmpData = new Variant(0, true);//该参数返回指纹模板数据
    //  Variant TmpLength = new Variant(0, true);//该参数返回指纹模板长度

		boolean result = zkem.invoke("SSR_DelUserTmp", v0,dwEnrollNumber,dwFingerIndex).getBoolean();


	}
	/**
	 * 查詢所有/指定ID的打卡信息;
	 *
	 * @param userNumber
	 * @return
	 */
	public static List<LogData> getUserOneDayInfo(Object userNumber) {
		ZkemSDK sdk = new ZkemSDK();
		Map<String, Object> userInfo = new HashMap<String, Object>();
		List<LogData> logDateList = new ArrayList<>();
		//连接考勤机；
		boolean connect = sdk.connect("192.168.1.189", 4370);
		if (connect) {
			List<Map<String, Object>> generalLogDataAll = ZkemSDK.getGeneralLogData();
			for (int i = 0; i < generalLogDataAll.size(); i++) {
				//System.out.println(generalLogDataAll.get(i));
				String Year = String.valueOf(generalLogDataAll.get(i).get("Year"));
				String Hour = String.valueOf(generalLogDataAll.get(i).get("Hour"));
				String InOutMode = String.valueOf(generalLogDataAll.get(i).get("InOutMode"));
				String Time = String.valueOf(generalLogDataAll.get(i).get("Time"));
				String Second = String.valueOf(generalLogDataAll.get(i).get("Second"));
				String Minute = String.valueOf(generalLogDataAll.get(i).get("Minute"));
				String EnrollNumber = String.valueOf(generalLogDataAll.get(i).get("EnrollNumber"));
				String Day = String.valueOf(generalLogDataAll.get(i).get("Day"));
				String Month = String.valueOf(generalLogDataAll.get(i).get("Month"));
				String VerifyMode = String.valueOf(generalLogDataAll.get(i).get("VerifyMode"));
				LogData logData = new LogData();
				logData.setYear(Year);
				logData.setHour(Hour);
				logData.setInOutMode1(InOutMode);
				logData.setTime(Time);
				logData.setSecond(Second);
				logData.setMinute(Minute);
				logData.setEnrollNumber(EnrollNumber);
				logData.setDay(Day);
				logData.setMonth(Month);
				logData.setVerifyMode(VerifyMode);
				if (EnrollNumber.equals(userNumber) && userNumber != null) {
					logDateList.add(logData);
				} else if (userNumber == null) {
					logDateList.add(logData);
				}
			}
			return logDateList;
		}
		return null;
	}

	/**
	 * 删除用户;
	 */
	public static Boolean delectUserById(String dwEnrollNumber) {
		Variant v0 = new Variant(1);
		Variant sdwEnrollNumber = new Variant(dwEnrollNumber, true);
		/**
		 * sdwBackupNumber：
		 * 一般范围为 0-9，同时会查询该用户是否还有其他指纹和密码，如都没有，则删除该用户
		 * 当为 10 是代表删除的是密码，同时会查询该用户是否有指纹数据，如没有，则删除该用户
		 * 11 和 13 是代表删除该用户所有指纹数据，
		 * 12 代表删除该用户（包括所有指纹和卡号、密码数据）
		 */
		Variant sdwBackupNumber = new Variant(12);
		/**
		 * 删除登记数据，和 SSR_DeleteEnrollData 不同的是删除所有指纹数据可用参数 13 实现，该函数具有更高效率
		 */
		return zkem.invoke("SSR_DeleteEnrollDataExt", v0, sdwEnrollNumber, sdwBackupNumber).getBoolean();
	}


	/**
	 * 考勤机链接测试
	 *
	 * @author wangchao
	 */
	public static void main(String[] args) {
		ZkemSDK sdk = new ZkemSDK();
		boolean connFlag = sdk.connect("192.168.1.189", 4370);
		System.out.println("conn:" + connFlag);
		boolean readGeneralLogData = sdk.readGeneralLogData();
		System.out.println("读取打卡机信息到缓存:" + readGeneralLogData);

		/*	sdk.getUserInfss(sdk.getUserInfs());*/
		//如果连接打卡机和读取信息到缓存都没问题再去缓存中获取数据
		if (connFlag || readGeneralLogData) {
			//获取打卡机所有的用户信息
			List<UserInfo> userInfo = sdk.getUserInfo();
			//转化为JSON字符串
			String usersJsonString = JSON.toJSONString(userInfo);
			//反序列化对象
			List<UserInfo> userInfos = JSON.parseArray(usersJsonString, UserInfo.class);
			userInfos.forEach(System.out::println);
			for (UserInfo info : userInfos) {
				String name = info.getName();
				Boolean enabled = info.getEnabled();
				String pwd = info.getPassword();
				Integer privilege = info.getPrivilege();
				String enrollNumber = info.getEnrollNumber();
				Integer userId = Integer.valueOf(enrollNumber);
				System.out.println(name + "-" + enabled + "-" + pwd + "-" + privilege + "-" + userId + "");
			}
			List<Map<String, Object>> generalLogData = sdk.getGeneralLogData();
			String logDataJsonString = JSON.toJSONString(generalLogData);
			List<LogData> logDatas = JSON.parseArray(logDataJsonString, LogData.class);
			logDatas.forEach(System.out::println);
			for (LogData data : logDatas) {
				//打卡机的时间
				String time = data.getTime();
				String enrollNumber = data.getEnrollNumber();

/*				  data有很多的数据,可以根据自己的业务逻辑拿想要的数据
				  我这里只取了打卡时间和enrollNumber*/
				System.out.println("时间:" + time + "            用户id:" + enrollNumber);
			}
		}
	}


	/**
	 * 启动事件监听
	 */
	public void  regEvent(){
		zkem.invoke("RegEvent", new Variant(1), new Variant(65535));
		zkem.invoke("ReadRTLog", new Variant(1));
		zkem.invoke("GetRTLog", new Variant(1));
		SensorEvents sensorEvents = new SensorEvents();
		new DispatchEvents(zkem.getObject(), sensorEvents);
		//开启监听方法
		new STA().doMessagePump();
//        new STA().doMessagePump();
	}
}
