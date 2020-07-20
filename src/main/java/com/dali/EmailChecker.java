package com.dali;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmailChecker {

    private static final int CONNECTION_TIMEOUT = 5 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;

    private static final String HELO_NAME = "relay.dali.com";
    private static final String HELO_ADDRESS = "dali@dali.com";

    private Logger log = LoggerFactory.getLogger(EmailChecker.class);

    private boolean debugEnabled = false;

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public boolean isDebugEnabled(){
        return this.debugEnabled;
    }

    public boolean checkEmail(String email) throws CheckerException {
        if(email == null || !email.contains("@")){
            throw new CheckerException("address malformed");
        }
        String[] mailParts = email.split("@");
        String mxDomain = this.getWeightedMXRecord(mailParts[1]);
        if(mxDomain == null){
            throw new CheckerException("mx record not found");
        }
        return checkWithDomainNameAndPort(mxDomain, 25, email);
    }


    public boolean checkWithDomainNameAndPort(String domainName, int port, String email) throws CheckerException {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(domainName, port), CONNECTION_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String firstLine = in.readLine();
            if(debugEnabled){
                log.info("welcome message:{}", firstLine);
            }

            String helloMessage = String.format("HELO %s\r\n", HELO_NAME);
            if(debugEnabled){
                log.info("HELO command:{}", helloMessage);
            }
            String helloResponse = sendCommand(helloMessage, out, in);
            if(debugEnabled){
                log.info("HELO response:{}", helloResponse);
            }

            String fromMessage = String.format("MAIL FROM:<%s>\r\n", HELO_ADDRESS);
            if(debugEnabled){
                log.info("FROM command:{}", fromMessage);
            }
            String fromResponse = sendCommand(fromMessage, out, in);
            if(debugEnabled){
                log.info("FROM response:{}", fromResponse);
            }

            String receiptMessage = String.format("RCPT TO: <%s>\r\n", email);
            if(debugEnabled){
                log.info("RCPT command:{}", receiptMessage);
            }
            String receiptResponse = sendCommand(receiptMessage, out, in);
            if(debugEnabled){
                log.info("RCPT response:{}", receiptResponse);
            }
            if(receiptResponse == null){
                if(debugEnabled){
                    log.info("RCPT response empty");
                }
                throw new CheckerException("RCPT response empty");
            }
            if( receiptResponse.startsWith("250")){
                return true;
            }else{
                return false;
            }

        } catch (IOException e) {
            throw new CheckerException(e.getMessage());
        }finally {
            try{
                if(out != null){
                    out.close();
                }
                if(in != null){
                    in.close();
                }
                if(socket != null){
                    socket.close();
                }
            }catch (Exception e){

            }
        }

    }

    public String getWeightedMXRecord(String domainName){
        try {
            Record[] records =  new Lookup(domainName, Type.MX).run();

            List<MXRecord> recordList = Arrays.stream(records).map(r-> (MXRecord)r).collect(Collectors.toList());
            if(recordList.size() == 0){
                if(debugEnabled){
                    log.info("no mx record found:{}", domainName);
                }
                return null;
            }
            MXRecord firstRecord = recordList.stream().min(Comparator.comparingInt(MXRecord::getPriority)).orElse(null);
            return firstRecord.getTarget().toString();
        } catch (TextParseException e) {
            if(debugEnabled){
                log.info("query mx record exception", e);
            }
            return null;
        }
    }

    private String sendCommand(String command, PrintWriter writer, BufferedReader reader){
        try{
            writer.print(command);
            writer.flush();
            return reader.readLine();
        }catch (Exception e){
            if(this.debugEnabled){
                log.warn("send command exception", e);
            }
            return null;
        }
    }

}
