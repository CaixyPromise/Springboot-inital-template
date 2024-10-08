<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="utf-8">
    <meta content="IE=edge" http-equiv="X-UA-Compatible">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${platformName} - 邮箱验证码</title>
</head>
<body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
<table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="width: 100%; min-width: 600px; margin: 0 auto; background-color: #ffffff; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);">
    <tr>
        <td style="padding: 20px 0; background-color: #6200ea;">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin: 0 auto;">
                <tr>
                    <td style="padding: 0 40px;">
                        <h1 style="color: #ffffff; font-size: 24px; margin: 0; white-space: nowrap;">${platformName}</h1>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
        <td style="padding: 40px 30px;">
            <h2 style="color: #333333; font-size: 18px; margin: 0 0 20px;">尊敬的用户：</h2>
            <p style="color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 20px;">您好！感谢您使用${platformName}。您的账号正在进行邮箱验证，验证码为：</p>
            <p style="font-size: 24px; font-weight: bold; color: #6200ea; text-align: center; margin: 30px 0;">${captcha}</p>
            <p style="color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 30px;">验证码有效期为5分钟，请尽快填写完成验证。</p>

            <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;">

            <h2 style="color: #333333; font-size: 18px; margin: 0 0 20px;">Dear User,</h2>
            <p style="color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 20px;">Hello! Thank you for using ${platformEnName}. Your account is being authenticated by email. The verification code is:</p>
            <p style="font-size: 24px; font-weight: bold; color: #6200ea; text-align: center; margin: 30px 0;">${captcha}</p>
            <p style="color: #555555; font-size: 16px; line-height: 1.5; margin: 0 0 30px;">The code is valid for 5 minutes. Please enter it as soon as possible to complete the verification.</p>
        </td>
    </tr>
    <tr>
        <td style="background-color: #f9f9f9; padding: 20px 30px;">
            <p style="color: #777777; font-size: 14px; line-height: 1.5; margin: 0 0 10px;">${platformResponsiblePerson} - ${platformName}</p>
            <p style="color: #777777; font-size: 14px; line-height: 1.5; margin: 0 0 10px;">
                此电子邮件仅限本人查看！如果有人要求你与他分享此电子邮件或验证码，或你认为误收此电子邮件，请联系我们：${platformContact}
                <br>
                This email is only for your personal use! If someone asks you to share this email or verification code with them, or if you think you have received this email by mistake, please contact us: ${platformContact}
            </p>
            <p style="color: #777777; font-size: 14px; line-height: 1.5; margin: 0;">此为系统邮件，请勿回复<br>Please do not reply to this system email</p>
        </td>
    </tr>
    <tr>
        <td style="background-color: #f0f0f0; padding: 20px 30px; text-align: center;">
            <p style="color: #999999; font-size: 12px; line-height: 1.5; margin: 0;">
                &copy; ${currentYear} ${platformName}. All rights reserved.
                <br>
                ${platformEnName} - ${platformContact}
            </p>
        </td>
    </tr>
</table>
</body>
</html>