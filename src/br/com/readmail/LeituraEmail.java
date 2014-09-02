package br.com.readmail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.velocity.VelocityContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class LeituraEmail {
	
	// Estrutura de pastas
	private String pastaBackup;
	
	private final static Logger logger = Logger.getLogger(LeituraEmail.class);
	private Properties properties;
	
	
	public static void main(String g[]) {
		LeituraEmail testeLeituraEmail = null;
		try {
			PropertyConfigurator.configure("log4j.properties");
			testeLeituraEmail = new LeituraEmail();
			testeLeituraEmail.processarEmail();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getMensagem(String chave) throws Exception {
		if(properties == null){
			properties = new Properties();
			
			InputStream is = new FileInputStream("leituraemail.properties");
			properties.load(is);
			is.close();
		}
		
		return properties.getProperty(chave);
	}
	
	public void processarEmail() throws Exception {
		PatternLayout patternLayout = new PatternLayout("%-4r [%t] %-5p %c %x - %m%n");
		FileAppender fileAppender = new FileAppender(patternLayout, this.getPastaBackup() + "log.txt");
		logger.addAppender(fileAppender);

		String host = getMensagem("HOST");
		String login = getMensagem("LOGIN");
		String senha = getMensagem("SENHA").trim();

		Session session = Session.getInstance(properties);
		Store store = session.getStore(getMensagem("PROTOCOLO"));
		store.connect(host, login, senha);

		
		Folder folderPastaPesquisa = null;
		folderPastaPesquisa = store.getFolder("INBOX");

		if (folderPastaPesquisa == null) {
			logger.error("Sem mensagens");
			System.exit(1);
		}
		folderPastaPesquisa.open(Folder.READ_WRITE);

		
		Message[] messages = folderPastaPesquisa.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
		logger.info("Total de emails com anexo não lidos: " + folderPastaPesquisa.getUnreadMessageCount() + "...");
		for (int i=0; i<messages.length; i++) {
			
			if(messages[i].getContent() instanceof Multipart){
				pesquisarHtml((Multipart) messages[i].getContent());
			}
		}
		logger.info("Fim do processamento.");
		folderPastaPesquisa.close(true);
		store.close();
	}

	private void pesquisarHtml(Multipart mp) throws MessagingException,
			IOException {
		for (int j = 0; j < mp.getCount(); j++) {
			if(mp.getBodyPart(j).isMimeType("text/html")){
				Document doc = Jsoup.parse((String) mp.getBodyPart(j).getContent());
				Elements elementsByTag = doc.getElementsByTag("table");
				
				element:
				for (org.jsoup.nodes.Element element : elementsByTag) {
					if(element.hasAttr("summary")){
						try {
							sendmail(element.getElementsByTag("tBody"));
						} catch (Exception e) {
							e.printStackTrace();
						}
						break element;
					}
				}
			}
		}
	}
	

	private void sendmail(Elements element) throws Exception {
		
		VelocityContext ctx = new VelocityContext();
		String cliente = null;
		String email = null;
		if(element.get(0).getElementsByTag("b").first().childNode(0) != null)
			cliente = element.get(0).getElementsByTag("b").first().childNode(0).toString();
		
		if(element.get(0).getElementsByTag("a").first().childNode(0) != null)
			email = element.get(0).getElementsByTag("a").first().childNode(0).toString();
		
		ctx.put("cliente", cliente);
		ctx.put("email", email);

		if(email != null){
			Email mail = new Email("br/com/readmail/resetarSenha.vm", "jea.aragao@hmit.com.br", ctx, properties);
			mail.setAssunto("test");
			mail.enviar();
		}
	}

	/**
	 * @return Returns the pastaBackup.
	 */
	public String getPastaBackup() {
		return pastaBackup;
	}

	/**
	 * @param pastaBackup
	 *            The pastaBackup to set.
	 */
	public void setPastaBackup(String pastaBackup) {
		this.pastaBackup = pastaBackup;
	}
}
