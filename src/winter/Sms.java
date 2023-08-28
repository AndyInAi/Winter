
package winter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 阿里云短信发送及查询
 * 
 */
public class Sms {

	public static String signName = "Winter"; // winter 短信签名

	public static String tempWorld = "SMS_23291xxxx"; // winter 国际模板

	public static String tempChina = "SMS_23289xxxx"; // winter 国内模板

	public static String accessKeyId = "LTAI6iNCxxxxxxxx";

	public static String accessKeySecret = "ODHfGPoPyW0mrMrZEBQQqAxxxxxxxx";

	public static String product = "Dysmsapi"; // 产品名称:云通信短信API产品,开发者无需替换

	public static String domain = "dysmsapi.aliyuncs.com"; // 产品域名,开发者无需替换

	public static void main(String[] args) throws ClientException, InterruptedException
	{

		Sms sms = new Sms();

		sms.test(); // 测试

	}

	private IAcsClient acsClient = null;

	public Sms()
	{

		// 可自助调整超时时间
		// System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		// System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret); // 初始化acsClient,暂不支持region化

		try {

			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);

		} catch (ClientException ex) {

			throw new RuntimeException(ex.getMessage());

		}

		acsClient = new DefaultAcsClient(profile);

	}

	/**
	 * 打印信息
	 * 
	 * @param o
	 */
	private void log(Object o)
	{

		String time = (new Timestamp(System.currentTimeMillis())).toString().substring(0, 19);

		System.out.println("[" + time + "] " + o.toString());

	}

	/**
	 * 查询发送结果
	 * 
	 * @param mobile 手机号码
	 * @return
	 */
	public QuerySendDetailsResponse query(String mobile)
	{

		return query(mobile, null);

	}

	/**
	 * 查询短信结果
	 * 
	 * @param mobile 手机号码
	 * @param bizId  流水号
	 * @return
	 */
	public QuerySendDetailsResponse query(String mobile, String bizId)
	{

		QuerySendDetailsRequest req = new QuerySendDetailsRequest();// 组装请求对象

		req.setPhoneNumber(mobile); // 必填-号码

		if (bizId != null && (bizId = bizId.trim()).length() > 0) {

			req.setBizId(bizId); // 可选-流水号

		}

		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd"); // 必填-发送日期
																// 支持30天内记录查询，格式yyyyMMdd

		req.setSendDate(ft.format(new Date()));

		req.setPageSize(10L); // 必填-页大小

		req.setCurrentPage(1L); // 必填-当前页码从1开始计数

		QuerySendDetailsResponse res = null;

		try {

			res = acsClient.getAcsResponse(req);

		} catch (ClientException ex) {

			log("查询短信结果出错：" + ex.getMessage());

		}

		return res;

	}

	/**
	 * 发送短信
	 * 
	 * @param mobile        手机号
	 * @param template      模板
	 * @param templateParam 模板参数
	 * @return 发送是否成功
	 */
	public boolean send(String mobile, String template, String templateParam)
	{

		SendSmsRequest req = new SendSmsRequest(); // 组装请求对象-具体描述见控制台-文档部分内容

		req.setPhoneNumbers(mobile); // 必填:待发送手机号

		req.setSignName(signName); // 必填:短信签名-可在短信控制台中找到

		req.setTemplateCode(template); // 必填:短信模板-可在短信控制台中找到

		if (templateParam != null) {

			req.setTemplateParam(templateParam); // 可选:模板中的变量替换JSON串

		}

		try {

			SendSmsResponse res = acsClient.getAcsResponse(req);

			if (res != null) {

				String code = res.getCode();

				if (code != null && code.trim().toUpperCase().equals("OK")) {

					return true;

				}

				log("发送短信出错：code=" + code + "----" + mobile);

			}

		} catch (ClientException ex) {

			System.out.println("mobile---------" + mobile);

			log("发送短信出错：" + ex.getMessage() + mobile);

		}

		return false;

	}

	/**
	 * 发送注册验证码
	 * 
	 * @param no   手机号
	 * @param code 注册验证码
	 * @return
	 */
	public boolean sendRegCode(String area, String no, String code)
	{

		String full = area.trim() + no.trim();

		if ("86".equals(area)) {

			return send(full, tempChina, "{\"code\":\"" + code + "\"}");

		}

		return send(full, tempWorld, "{\"code\":\"" + code + "\"}");

	}

	/**
	 * 休眠
	 * 
	 * @param millis 毫秒
	 */
	@SuppressWarnings("unused")
	private void sleep(long millis)
	{

		try {

			Thread.sleep(millis);

		} catch (InterruptedException ex) {

			log("休眠出错：" + ex.getMessage());

		}

	}

	/**
	 * 测试 香港号码 852-96xxxxxx
	 */
	private void test()
	{

		String area = "86"; // "852";
		String mobile = "130xxxxxxxx"; // "96xxxxxx";

		boolean res = sendRegCode(area, mobile, "888888");

		log(res ? "发送成功" : "发送失败");

	}

}
