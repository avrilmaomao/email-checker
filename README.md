# Email-checker
# When sending an email, it's important to make sure the address really exists, especially marketing emails when we can't verify the reciever with a link or a code.
Currently we are using SMTP protocol（without actually sending) to check the server response for the RCPT command. 
This approach is accurate for some mail providers, but inaccurate or has some limits for others.
So DO NOT use it in situations where it need to be the exact result.


## How to check
EmailChecker checker = new EmailChecker();
boolean checkOk = checker.checkEmail("dali@gmail.com");

During the check proess, if encounted with some errors, it will throw a CheckerException and the check result can be considered as unkown.

