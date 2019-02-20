package com.we.weblog.domain.util;

import com.we.weblog.domain.Comment;
import org.springframework.scheduling.annotation.Async;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 发送邮件的工具类
 * @Auther: JohnDoeo
 * @Date: 2019/1/14 23:40
 * @Description:
 *
 * 使用此类需要先引入maven
 *      <dependency>
 *             <groupId>com.sun.mail</groupId>
 *             <artifactId>mailapi</artifactId>
 *             <version>1.6.0</version>
 *         </dependency>
 * 		<dependency>
 * 			<groupId>com.sun.mail</groupId>
 * 			<artifactId>smtp</artifactId>
 * 			<version>1.6.1</version>
 * 		</dependency>
 */
public class SendMailWithFile {
    //发件人地址
    public static String senderAddress = "18829348962@163.com";
    //收件人地址
    public static String recipientAddress = "18829348962@163.com";
    //发件人账户名
    public static String senderAccount = "18829348962@163.com";
    //发件人账户密码
    public static String senderPassword = "dhy199409155476";

    private static String defaultFilePath = new SendMailWithFile().getDefaultFilePath()+"\\defaultPic.png";
    private static String defaultSubject = "访问通知";
    private static String defaultContent = "不知是哪个老铁又访问博客了！！！";

    public String getDefaultFilePath(){
        String userDir = System.getProperty("user.dir") ;
        Path path = Paths.get(userDir  , "src" , "main" , "resources","static") ;
        return path.toString();
    }

    public static void main(String[] a){
//        sendMail();
    }

    /**
     * 提醒我有人评论了博客
     * @param comment
     */
    public static void sendMail(Comment comment,String postName){
        String content = comment.getAuthor()+"("+comment.getEmail()+")评论了您的博客（"+postName+"）,"+"<br/>评论内容为："+comment.getContent()+
                "<br/>评论时间："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sendMail("评论提醒",content,recipientAddress);
    }

    public static void sendMail(){
        sendMail(defaultSubject,defaultContent,recipientAddress);
    }

    public static void sendMail(String subject,String content, String recipientAddress){
        new SendMailWithFile().sendMail(defaultFilePath,subject,content,recipientAddress);
    }
    @Async//异步发送邮件
    public void sendMail(String filePath,String subject,String content, String recipientAddress){
        Transport transport = null;
        try {
            //1、连接邮件服务器的参数配置
            Properties props = new Properties();
            //设置用户的认证方式
            props.setProperty("mail.smtp.auth", "true");
            //设置传输协议
            props.setProperty("mail.transport.protocol", "smtp");
            //设置发件人的SMTP服务器地址
            props.setProperty("mail.smtp.host", "smtp.163.com");
            props.setProperty("mail.smtp.port","465");
            //设置ssl加密
            props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.setProperty("mail.smtp.socketFactory.port", "465");
            //2、创建定义整个应用程序所需的环境信息的 Session 对象
            Session session = Session.getInstance(props);
            //设置调试信息在控制台打印出来
            session.setDebug(true);
            //3、创建邮件的实例对象
            Message msg = getMimeMessage(session,filePath,subject,content,senderAddress,recipientAddress);
            //4、根据session对象获取邮件传输对象Transport
            transport = session.getTransport();
            //设置发件人的账户名和密码
            transport.connect(senderAccount, senderPassword);
            //发送邮件，并发送到所有收件人地址，message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(msg,msg.getAllRecipients());

            //将创建的Email写入到E盘存储
//        msg.writeTo(new FileOutputStream("D:\\attachMail.eml"));
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                //5、关闭邮件连接
                transport.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取邮件的实例对象
     * @param session
     * @param filePath
     * @param subject
     * @param content
     * @param senderAddress
     * @param recipientAddress
     * @return
     * @throws Exception
     */
    public static MimeMessage getMimeMessage(Session session,String filePath,String subject,String content,String senderAddress, String recipientAddress) throws Exception{
        //1.创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(session);
        //2.设置发件人地址
        msg.setFrom(new InternetAddress(senderAddress));
        /**
         * 3.设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
        //4.设置邮件主题
        msg.setSubject(subject,"UTF-8");

        //下面是设置邮件正文
        msg.setContent(content, "text/html;charset=UTF-8");

        // 5. 创建图片"节点"
        MimeBodyPart image = new MimeBodyPart();
        // 读取本地文件
        DataHandler dh = new DataHandler(new FileDataSource(filePath));
        // 将图片数据添加到"节点"
        image.setDataHandler(dh);
        // 为"节点"设置一个唯一编号（在文本"节点"将引用该ID）
        image.setContentID("mailTestPic");

        // 6. 创建文本"节点"
        MimeBodyPart text = new MimeBodyPart();
        // 这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
        text.setContent(content+"<br/><a href='http://www.cnblogs.com/ysocean/p/7666061.html'><img src='cid:mailTestPic'/></a><br/>"+"感谢！", "text/html;charset=UTF-8");

        // 7. （文本+图片）设置 文本 和 图片"节点"的关系（将 文本 和 图片"节点"合成一个混合"节点"）
        MimeMultipart mm_text_image = new MimeMultipart();
        mm_text_image.addBodyPart(text);
        mm_text_image.addBodyPart(image);
        mm_text_image.setSubType("related");    // 关联关系

        // 8. 将 文本+图片 的混合"节点"封装成一个普通"节点"
        // 最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
        // 上面的 mailTestPic 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
        MimeBodyPart text_image = new MimeBodyPart();
        text_image.setContent(mm_text_image);

        /*添加附件节点
        // 9. 创建附件"节点"
        MimeBodyPart attachment = new MimeBodyPart();
        // 读取本地文件
        DataHandler dh2 = new DataHandler(new FileDataSource("C:\\Users\\Administrator\\Desktop\\update.sh"));
        // 将附件数据添加到"节点"
        attachment.setDataHandler(dh2);
        // 设置附件的文件名（需要编码）
        attachment.setFileName(MimeUtility.encodeText(dh2.getName()));*/

        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合"节点" / Multipart ）
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text_image);
//        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
        mm.setSubType("mixed");         // 混合关系

        // 11. 设置整个邮件的关系（将最终的混合"节点"作为邮件的内容添加到邮件对象）
        msg.setContent(mm);
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());

        return msg;
    }

}
