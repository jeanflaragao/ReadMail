package br.com.readmail;

import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * @author andrea.menezes
 */
public class Email
{
	static Logger logger = Logger.getLogger(Email.class);

	private String destinatario;

	private String destinatarioCopia;

	private String assunto = "";

	private String path;

	private StringWriter corpo;

	private VelocityContext context;

	private Properties props;

	public Email(String path, String destinatario, VelocityContext context, Properties prop)
	{
		this.path = path;
		this.destinatario = destinatario;
		this.context = context;
		this.props = prop;
	}

	public void enviar() throws Exception
	{
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("aragao.jean@gmail.com","Je180485");
				}
			});
 
		 
		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress("from@no-spam.com"));
		message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse("to@no-spam.com"));
		message.setSubject("Testing Subject");
		
		message.setSentDate(new Date());
		
		this.formatarCorpo();
		message.setContent(this.corpo.toString(), "text/html;charset=utf-8");
		
		Transport.send(message);
		System.out.println("Done");
	}

	private void formatarCorpo()
	{
		VelocityEngine ve = new VelocityEngine();

		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
		ve.setProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		ve.setProperty("input.encoding", "UTF-8");
		ve.setProperty("output.encoding", "UTF-8");
		ve.init();

		/* get the Template */
		Template t = ve.getTemplate(this.path);
		this.corpo = new StringWriter();
		t.merge(context, corpo);
	}

	public String getDestinatario()
	{
		return destinatario;
	}

	public void setDestinatario(String destinatario)
	{
		this.destinatario = destinatario;
	}

	public String getDestinatarioCopia()
	{
		return destinatarioCopia;
	}

	public void setDestinatarioCopia(String destinatarioCopia)
	{
		this.destinatarioCopia = destinatarioCopia;
	}

	public String getAssunto()
	{
		return assunto;
	}

	public void setAssunto(String assunto)
	{
		this.assunto = assunto;
	}

	public String getpath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}
}