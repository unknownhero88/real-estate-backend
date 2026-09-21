package com.example.demo.util;

public class EmailUtils {

    // ─────────────────────────────────────────
    // PREVENT INSTANTIATION
    // Utility class — all methods are static
    // ─────────────────────────────────────────
    private EmailUtils() {}

    // ─────────────────────────────────────────
    // BUILD VERIFICATION EMAIL HTML
    // ─────────────────────────────────────────
    public static String buildVerificationEmail(String name,
                                                String verifyLink) {
        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "  <meta charset='UTF-8'/>" +
            "  <meta name='viewport' content='width=device-width'/>" +
            "  <title>Verify Your Email</title>" +
            "</head>" +
            "<body style='" +
            "  margin:0; padding:0;" +
            "  background-color:#f4f7f9;" +
            "  font-family: Arial, sans-serif;'>" +

            // ── Wrapper ──
            "  <table width='100%' cellpadding='0'" +
            "         cellspacing='0' border='0'" +
            "         style='background-color:#f4f7f9; padding: 40px 0;'>" +
            "    <tr><td align='center'>" +

            // ── Card ──
            "      <table width='600' cellpadding='0'" +
            "             cellspacing='0' border='0'" +
            "             style='" +
            "               background-color:#ffffff;" +
            "               border-radius:12px;" +
            "               overflow:hidden;" +
            "               box-shadow: 0 4px 20px rgba(0,0,0,0.08);'>" +

            // ── Header ──
            "        <tr>" +
            "          <td style='" +
            "            background: linear-gradient(" +
            "              135deg, #16a34a, #15803d);" +
            "            padding: 40px 40px 30px;" +
            "            text-align: center;'>" +
            "            <h1 style='" +
            "              color:#ffffff;" +
            "              font-size:28px;" +
            "              margin:0;" +
            "              font-weight:700;" +
            "              letter-spacing:-0.5px;'>" +
            "              🏠 RealEstate Portal" +
            "            </h1>" +
            "            <p style='" +
            "              color:rgba(255,255,255,0.85);" +
            "              margin:8px 0 0;" +
            "              font-size:14px;'>" +
            "              Find Your Dream Home" +
            "            </p>" +
            "          </td>" +
            "        </tr>" +

            // ── Body ──
            "        <tr>" +
            "          <td style='padding: 40px;'>" +

            // Greeting
            "            <p style='" +
            "              color:#1f2937;" +
            "              font-size:18px;" +
            "              font-weight:600;" +
            "              margin:0 0 8px;'>" +
            "              Hi " + name + "! 👋" +
            "            </p>" +
            "            <p style='" +
            "              color:#6b7280;" +
            "              font-size:15px;" +
            "              line-height:1.6;" +
            "              margin:0 0 30px;'>" +
            "              Thank you for registering with" +
            "              RealEstate Portal. Please verify" +
            "              your email address to activate" +
            "              your account." +
            "            </p>" +

            // Verify Button
            "            <table width='100%' cellpadding='0'" +
            "                   cellspacing='0' border='0'>" +
            "              <tr><td align='center'" +
            "                      style='padding: 10px 0 30px;'>" +
            "                <a href='" + verifyLink + "'" +
            "                   style='" +
            "                     display:inline-block;" +
            "                     background-color:#16a34a;" +
            "                     color:#ffffff;" +
            "                     text-decoration:none;" +
            "                     padding:16px 40px;" +
            "                     border-radius:8px;" +
            "                     font-size:16px;" +
            "                     font-weight:600;" +
            "                     letter-spacing:0.3px;'>" +
            "                  ✅ Verify Email Address" +
            "                </a>" +
            "              </td></tr>" +
            "            </table>" +

            // Divider
            "            <hr style='" +
            "              border:none;" +
            "              border-top:1px solid #e5e7eb;" +
            "              margin:0 0 24px;'/>" +

            // Link fallback
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:13px;" +
            "              line-height:1.6;" +
            "              margin:0 0 8px;'>" +
            "              Or copy and paste this link:" +
            "            </p>" +
            "            <p style='" +
            "              color:#16a34a;" +
            "              font-size:13px;" +
            "              word-break:break-all;" +
            "              margin:0 0 24px;'>" +
            "              " + verifyLink +
            "            </p>" +

            // Expiry warning
            "            <div style='" +
            "              background-color:#fef9c3;" +
            "              border-left:4px solid #eab308;" +
            "              border-radius:6px;" +
            "              padding:14px 16px;" +
            "              margin-bottom:24px;'>" +
            "              <p style='" +
            "                color:#713f12;" +
            "                font-size:13px;" +
            "                margin:0;'>" +
            "                ⏰ This link will expire in" +
            "                <strong>24 hours</strong>." +
            "                If expired, please register again." +
            "              </p>" +
            "            </div>" +

            // Not you note
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:13px;" +
            "              margin:0;'>" +
            "              If you did not create this account," +
            "              you can safely ignore this email." +
            "            </p>" +

            "          </td>" +
            "        </tr>" +

            // ── Footer ──
            "        <tr>" +
            "          <td style='" +
            "            background-color:#f9fafb;" +
            "            padding:24px 40px;" +
            "            text-align:center;" +
            "            border-top:1px solid #e5e7eb;'>" +
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:12px;" +
            "              margin:0 0 6px;'>" +
            "              © " + java.time.Year.now().getValue() +
            "              RealEstate Portal. All rights reserved." +
            "            </p>" +
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:12px;" +
            "              margin:0;'>" +
            "              123 Real Estate Tower, Bhopal, MP — 462001" +
            "            </p>" +
            "          </td>" +
            "        </tr>" +

            "      </table>" +
            "    </td></tr>" +
            "  </table>" +
            "</body>" +
            "</html>";
    }

    // ─────────────────────────────────────────
    // BUILD PASSWORD RESET EMAIL HTML
    // ─────────────────────────────────────────
    public static String buildPasswordResetEmail(String name,
                                                 String resetLink) {
        return "<!DOCTYPE html>" +
            "<html lang='en'>" +
            "<head>" +
            "  <meta charset='UTF-8'/>" +
            "  <meta name='viewport' content='width=device-width'/>" +
            "  <title>Reset Your Password</title>" +
            "</head>" +
            "<body style='" +
            "  margin:0; padding:0;" +
            "  background-color:#f4f7f9;" +
            "  font-family: Arial, sans-serif;'>" +

            // ── Wrapper ──
            "  <table width='100%' cellpadding='0'" +
            "         cellspacing='0' border='0'" +
            "         style='background-color:#f4f7f9; padding:40px 0;'>" +
            "    <tr><td align='center'>" +

            // ── Card ──
            "      <table width='600' cellpadding='0'" +
            "             cellspacing='0' border='0'" +
            "             style='" +
            "               background-color:#ffffff;" +
            "               border-radius:12px;" +
            "               overflow:hidden;" +
            "               box-shadow:0 4px 20px rgba(0,0,0,0.08);'>" +

            // ── Header ──
            "        <tr>" +
            "          <td style='" +
            "            background:linear-gradient(" +
            "              135deg, #dc2626, #b91c1c);" +
            "            padding:40px 40px 30px;" +
            "            text-align:center;'>" +
            "            <h1 style='" +
            "              color:#ffffff;" +
            "              font-size:28px;" +
            "              margin:0;" +
            "              font-weight:700;'>" +
            "              🔒 Password Reset" +
            "            </h1>" +
            "            <p style='" +
            "              color:rgba(255,255,255,0.85);" +
            "              margin:8px 0 0;" +
            "              font-size:14px;'>" +
            "              RealEstate Portal" +
            "            </p>" +
            "          </td>" +
            "        </tr>" +

            // ── Body ──
            "        <tr>" +
            "          <td style='padding:40px;'>" +

            // Greeting
            "            <p style='" +
            "              color:#1f2937;" +
            "              font-size:18px;" +
            "              font-weight:600;" +
            "              margin:0 0 8px;'>" +
            "              Hi " + name + "!" +
            "            </p>" +
            "            <p style='" +
            "              color:#6b7280;" +
            "              font-size:15px;" +
            "              line-height:1.6;" +
            "              margin:0 0 30px;'>" +
            "              We received a request to reset your" +
            "              password. Click the button below to" +
            "              create a new password." +
            "            </p>" +

            // Reset Button
            "            <table width='100%' cellpadding='0'" +
            "                   cellspacing='0' border='0'>" +
            "              <tr><td align='center'" +
            "                      style='padding:10px 0 30px;'>" +
            "                <a href='" + resetLink + "'" +
            "                   style='" +
            "                     display:inline-block;" +
            "                     background-color:#dc2626;" +
            "                     color:#ffffff;" +
            "                     text-decoration:none;" +
            "                     padding:16px 40px;" +
            "                     border-radius:8px;" +
            "                     font-size:16px;" +
            "                     font-weight:600;'>" +
            "                  🔑 Reset My Password" +
            "                </a>" +
            "              </td></tr>" +
            "            </table>" +

            // Divider
            "            <hr style='" +
            "              border:none;" +
            "              border-top:1px solid #e5e7eb;" +
            "              margin:0 0 24px;'/>" +

            // Link fallback
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:13px;" +
            "              margin:0 0 8px;'>" +
            "              Or copy and paste this link:" +
            "            </p>" +
            "            <p style='" +
            "              color:#dc2626;" +
            "              font-size:13px;" +
            "              word-break:break-all;" +
            "              margin:0 0 24px;'>" +
            "              " + resetLink +
            "            </p>" +

            // Expiry warning
            "            <div style='" +
            "              background-color:#fee2e2;" +
            "              border-left:4px solid #dc2626;" +
            "              border-radius:6px;" +
            "              padding:14px 16px;" +
            "              margin-bottom:24px;'>" +
            "              <p style='" +
            "                color:#7f1d1d;" +
            "                font-size:13px;" +
            "                margin:0;'>" +
            "                ⏰ This link will expire in" +
            "                <strong>30 minutes</strong>." +
            "                Request a new one if expired." +
            "              </p>" +
            "            </div>" +

            // Security note
            "            <div style='" +
            "              background-color:#f0f9ff;" +
            "              border-left:4px solid #0ea5e9;" +
            "              border-radius:6px;" +
            "              padding:14px 16px;" +
            "              margin-bottom:24px;'>" +
            "              <p style='" +
            "                color:#0c4a6e;" +
            "                font-size:13px;" +
            "                margin:0;'>" +
            "                🛡️ If you did not request a password" +
            "                reset, please ignore this email." +
            "                Your password will remain unchanged." +
            "              </p>" +
            "            </div>" +

            "          </td>" +
            "        </tr>" +

            // ── Footer ──
            "        <tr>" +
            "          <td style='" +
            "            background-color:#f9fafb;" +
            "            padding:24px 40px;" +
            "            text-align:center;" +
            "            border-top:1px solid #e5e7eb;'>" +
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:12px;" +
            "              margin:0 0 6px;'>" +
            "              © " + java.time.Year.now().getValue() +
            "              RealEstate Portal. All rights reserved." +
            "            </p>" +
            "            <p style='" +
            "              color:#9ca3af;" +
            "              font-size:12px;" +
            "              margin:0;'>" +
            "              123 Real Estate Tower, Bhopal, MP — 462001" +
            "            </p>" +
            "          </td>" +
            "        </tr>" +

            "      </table>" +
            "    </td></tr>" +
            "  </table>" +
            "</body>" +
            "</html>";
    }

    // ─────────────────────────────────────────
    // VALIDATE EMAIL FORMAT
    // ─────────────────────────────────────────
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String emailRegex = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    // ─────────────────────────────────────────
    // MASK EMAIL FOR DISPLAY
    // example: j***@gmail.com
    // ─────────────────────────────────────────
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts  = email.split("@");
        String   local  = parts[0];
        String   domain = parts[1];

        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }

        return local.charAt(0) +
               "***" +
               local.charAt(local.length() - 1) +
               "@" + domain;
    }
}