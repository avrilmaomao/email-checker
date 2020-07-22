## Email-checker
Check if an email address really exists. 

When sending an email, it's important to make sure the address really exists, especially marketing emails when we can't verify the reciever with a link or a code.

Currently we are using SMTP protocol（without actually sending) to check the server response for the RCPT command. 

This approach is accurate for some mail providers, but inaccurate or has some limits for others.

So DO NOT use it in situations where it need to be the exact result.


## How to check

    EmailChecker checker = new EmailChecker();
    
    boolean checkOk = checker.checkEmail("dali@gmail.com");

During the checking process, if encounted with  errors, it will throw a CheckerException and the check result can be considered as unkown.


## Limitations

Different mail service providers have different rules when answering the RCPT command, which leads to the inacurate result of this method.

Some mail providers don't check the validity during the RCPT command at all, some just check if the domain is valid, others do check the if full address exists.So when you get a True result, it dosen't mean the address really exists.

However, if you get a False result, it's highly possible the address  doesn't exist, since the mail service provider needs to do a real check to give a negative result.

Different Providers and True Result Accuracy

|Mail Service Provider|True Result Accuracy|
|-:|:-|
|Gmail|Accurate|
|QQ Mail|Not Accurate|
|Tencent Enterprise Mail|not accurate, but domain exists|
|Aliyun Enterprise Mail|Accurate|
