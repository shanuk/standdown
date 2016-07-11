import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPersona;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackConnected;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.SlackUserChange;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackUserChangeListener;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Created by sgumirov on 6/7/16.
 */
public class ConnectAndListen
{

  private static final String PROMPT_1 = "1. What did you do yesterday?";
  private static final String PROMPT_2 = "2. What are you working on today?";
  private static final String PROMPT_3 = "3. Is there anything standing in your way?";
  private static final String PROMPT_YES = "Welcome to daily standup! Are you ready to begin? ('-yes', or '-skip')";

  private SlackPersona me;
  private States expectedMsg = States.STATE_YES;
  private LogForm logform = new LogForm();

  Semaphore semaphore = new Semaphore(1);

  enum States{
    STATE_YES,
    STATE_1,STATE_2,STATE_3
  }

  private void log (String s)
  {
    if (logform != null) logform.append(s);
    System.out.println(s);
  }

  public ConnectAndListen(final String yesterdayReport, final String todayReport, final String blocking, final String token)
      throws IOException, InterruptedException {
    logform.init();
    semaphore.acquire();
    final SlackSession ses = SlackSessionFactory.createWebSocketSlackSession(token);
    ses.addSlackConnectedListener(new SlackConnectedListener() {
      @Override
      public void onEvent(SlackConnected event, SlackSession session) {
        me = event.getConnectedPersona();
        log("Connected");
      }
    });
    ses.addSlackUserChangeListener(new SlackUserChangeListener() {
      @Override
      public void onEvent(SlackUserChange event, SlackSession session) {
//        log("user changed: event="+event);
      }
    });
    try {
      ses.connect();
      logform.setDisconnectObserver(new Observer() {
        @Override
        public void update(Observable o, Object arg) {
          semaphore.release();
        }
      });
      ses.addMessagePostedListener(new SlackMessagePostedListener() {
        @Override
        public void onEvent(SlackMessagePosted event, SlackSession session) {
          String msg = event.getMessageContent();
          log("onEvent(): incoming="+event);
  //        log("onEvent(): channelID="+event.getChannel().getId()+" chName="+event.getChannel().getName()+" msg=["+msg+"]");
          SlackChannel ch = event.getChannel();
          if (!msg.contains(me.getId())) {
            log("MSG not for me");
            return;
          }
          if (expectedMsg == States.STATE_YES && msg.contains(PROMPT_YES)){
            ses.sendMessage(ch, "-yes");
            expectedMsg = States.STATE_1;
          }
          else if (expectedMsg == States.STATE_1 && msg.contains(PROMPT_1)){
            ses.sendMessage(ch, yesterdayReport);
            expectedMsg = States.STATE_2;
          }
          else if (expectedMsg == States.STATE_2 && msg.contains(PROMPT_2)){
            ses.sendMessage(ch, todayReport);
            expectedMsg = States.STATE_3;
          }
          else if (expectedMsg == States.STATE_3 && msg.contains(PROMPT_3)){
            ses.sendMessage(ch, blocking);
            JOptionPane.showMessageDialog(logform.getFrame(), "Answered all questions. Please check in channel");
            log("releasing semaphore.");
            semaphore.release(); //exit
          }
        }
      });
      semaphore.acquire();
      log("Shutting down");
      ses.disconnect();
    } catch (Exception e) {
      e.printStackTrace();
      log("Error while connecting: "+e.getMessage());
      JOptionPane.showMessageDialog(logform.getFrame(), "Error while connecting, see details:\n"+e.getMessage());
    }
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Properties conf = new Properties();
    try{
      conf.load(new FileInputStream("config.txt"));
    }catch(IOException e){
    }
    initWithUI(conf);
  }

  private static void initWithUI(final Properties conf) {
    new QuestionsForm().init(conf).setListener(new Observer() {
      @Override
      public void update(Observable o, Object arg) {
        Map<String,String> map = (Map<String, String>) arg;
        for (String k : map.keySet()){
          conf.put(k, map.get(k));
        }
        try {
          conf.store(new FileOutputStream("config.txt"), "");
        } catch (IOException e) {
          System.err.println("cannot store config: "+e.getMessage());
          e.printStackTrace();
        }
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              new ConnectAndListen(conf.getProperty(QuestionsForm.Q1), conf.getProperty(QuestionsForm.Q2), conf.getProperty(QuestionsForm.Q3),
                  conf.getProperty(QuestionsForm.TOKEN));
            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        }).start();
      }
    });
  }
}
