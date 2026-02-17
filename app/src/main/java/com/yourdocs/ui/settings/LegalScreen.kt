package com.yourdocs.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.yourdocs.ui.components.GradientTopBar

enum class LegalType {
    PRIVACY_POLICY,
    TERMS_OF_SERVICE
}

@Composable
fun LegalScreen(
    legalType: LegalType,
    onNavigateBack: () -> Unit
) {
    val title = when (legalType) {
        LegalType.PRIVACY_POLICY -> "Privacy Policy"
        LegalType.TERMS_OF_SERVICE -> "Terms of Service"
    }

    Scaffold(
        topBar = {
            GradientTopBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (legalType) {
                LegalType.PRIVACY_POLICY -> PrivacyPolicyContent()
                LegalType.TERMS_OF_SERVICE -> TermsOfServiceContent()
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacyPolicyContent() {
    LegalMeta("Effective Date: February 17, 2026")

    LegalParagraph(
        "YourDocs Team (\"we\", \"us\", or \"our\") operates the YourDocs mobile application (the \"App\"). " +
                "This Privacy Policy explains how we handle information when you use the App."
    )

    LegalHeading("1. Local-Only Document Storage")
    LegalParagraph(
        "All documents, folders, and associated data you create or import in the App are stored locally on your device. " +
                "Your documents never leave your device unless you explicitly choose to share or export them."
    )

    LegalHeading("2. No Data Collection or Transmission")
    LegalParagraph(
        "The App does not collect, transmit, or store any personal data on external servers. " +
                "We do not operate any servers or cloud services that receive your data. " +
                "Your documents and organizational data remain entirely on your device."
    )

    LegalHeading("3. Biometric and PIN Data")
    LegalParagraph(
        "The App offers biometric authentication (fingerprint/face) and PIN-based locking for folders. " +
                "Biometric authentication is handled entirely by the Android operating system through the AndroidX Biometric library. " +
                "We never access, store, or transmit your biometric data. " +
                "PIN values are hashed locally on your device and are never stored in plain text or transmitted anywhere."
    )

    LegalHeading("4. Backup and Restore")
    LegalParagraph(
        "The App allows you to create backup files containing your folders and documents. " +
                "These backup files are created and stored locally on your device. " +
                "Sharing or transferring backup files is initiated solely by you through your device's share functionality. " +
                "We have no access to your backup files."
    )

    LegalHeading("5. Payment Information")
    LegalParagraph(
        "Premium features are purchased through Google Play Billing. All payment processing is handled entirely by Google Play. " +
                "The App does not collect, store, or have access to any payment information, " +
                "including credit card numbers, billing addresses, or other financial data. " +
                "Please refer to Google's Privacy Policy for information on how Google handles payment data."
    )

    LegalHeading("6. No Analytics or Tracking")
    LegalParagraph(
        "The App does not use any analytics services, advertising SDKs, crash reporting tools, or tracking technologies. " +
                "We do not collect usage data, device identifiers, or any form of telemetry."
    )

    LegalHeading("7. Third-Party Services")
    LegalParagraph(
        "The only third-party service integrated into the App is Google Play Billing for processing premium purchases. " +
                "The App also uses Google ML Kit's on-device document scanner, " +
                "which processes images locally on your device without sending data to external servers."
    )

    LegalHeading("8. Children's Privacy")
    LegalParagraph(
        "The App does not knowingly collect any personal information from anyone, including children under the age of 13. " +
                "Since the App does not collect any data, no special provisions for children's data are necessary."
    )

    LegalHeading("9. Changes to This Privacy Policy")
    LegalParagraph(
        "We may update this Privacy Policy from time to time. Changes will be communicated through App updates on Google Play. " +
                "Your continued use of the App after any changes constitutes acceptance of the updated Privacy Policy. " +
                "We encourage you to review this policy periodically."
    )

    LegalHeading("10. Contact Us")
    LegalParagraph("If you have any questions or concerns about this Privacy Policy, please contact us at:")
    LegalContact()
}

@Composable
private fun TermsOfServiceContent() {
    LegalMeta("Effective Date: February 17, 2026")

    LegalParagraph(
        "Please read these Terms of Service (\"Terms\") carefully before using the YourDocs mobile application (the \"App\") " +
                "operated by YourDocs Team (\"we\", \"us\", or \"our\")."
    )
    LegalParagraph(
        "By using the App, you agree to be bound by these Terms. If you do not agree to these Terms, do not use the App."
    )

    LegalHeading("1. Description of Service")
    LegalParagraph(
        "YourDocs is a mobile application for personal document organization. " +
                "The App allows you to create folders, import documents, and organize files locally on your device."
    )

    LegalHeading("2. Premium Purchase")
    LegalSubheading("2.1 One-Time Purchase")
    LegalParagraph(
        "The App offers a one-time premium upgrade (\"YourDocs Pro\") that unlocks additional features. " +
                "This is a single, non-recurring digital purchase processed through Google Play."
    )
    LegalSubheading("2.2 Refund Policy")
    LegalParagraph(
        "All sales are final after the Google Play refund window has expired. " +
                "Refund requests within the eligible window should be directed to Google Play. " +
                "YourDocs Team does not process refunds directly."
    )
    LegalSubheading("2.3 Premium Features")
    LegalParagraph(
        "Premium features currently include unlimited folders and documents, biometric and PIN folder locking, " +
                "backup and restore functionality, and custom folder colors. " +
                "Premium features may be updated, expanded, or modified over time. " +
                "We reserve the right to add new premium features in future updates at no additional cost to existing Pro users."
    )

    LegalHeading("3. Free Tier Limitations")
    LegalParagraph(
        "Free users may create up to 5 folders and store up to 25 documents. These limits may be adjusted in future updates. " +
                "Existing data that exceeds any updated limits will not be deleted; " +
                "only the creation of new items beyond the limit will be restricted."
    )

    LegalHeading("4. User Responsibilities")
    LegalSubheading("4.1 Backups")
    LegalParagraph(
        "You are solely responsible for maintaining backups of your documents and data. " +
                "We strongly recommend creating regular backups of your important documents using the App's backup feature or other means."
    )
    LegalSubheading("4.2 Document Content")
    LegalParagraph(
        "You are solely responsible for the content of documents you store in the App. " +
                "The App is intended for personal document organization only. " +
                "You agree not to use the App for any unlawful purpose."
    )
    LegalSubheading("4.3 Device Security")
    LegalParagraph(
        "You are responsible for maintaining the security of your device, " +
                "including any PINs or biometric access configured within the App."
    )

    LegalHeading("5. Disclaimer of Warranties")
    LegalParagraph(
        "THE APP IS PROVIDED ON AN \"AS IS\" AND \"AS AVAILABLE\" BASIS WITHOUT WARRANTIES OF ANY KIND, " +
                "EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO IMPLIED WARRANTIES OF MERCHANTABILITY, " +
                "FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT."
    )
    LegalParagraph(
        "YourDocs Team does not warrant that the App will be uninterrupted, error-free, or free of harmful components. " +
                "We do not warrant that any data stored in the App will be preserved indefinitely or protected from loss."
    )

    LegalHeading("6. Limitation of Liability")
    LegalParagraph(
        "TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, YOURDOCS TEAM SHALL NOT BE LIABLE FOR ANY " +
                "INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, OR ANY LOSS OF DATA, DOCUMENTS, " +
                "PROFITS, OR REVENUE, WHETHER INCURRED DIRECTLY OR INDIRECTLY, OR ANY LOSS OF DATA OR DOCUMENT CORRUPTION, " +
                "REGARDLESS OF THE CAUSE OF ACTION OR THE THEORY OF LIABILITY."
    )
    LegalParagraph(
        "This includes, but is not limited to, any data loss resulting from device failure, App updates, " +
                "uninstallation, or any other circumstance. " +
                "You acknowledge that maintaining backups of your important documents is your responsibility."
    )

    LegalHeading("7. Intended Use")
    LegalParagraph(
        "The App is intended for personal document organization only. " +
                "It is not designed or intended for use as a primary document storage solution, " +
                "a secure vault for highly sensitive documents, or a replacement for professional document management systems."
    )

    LegalHeading("8. Intellectual Property")
    LegalParagraph(
        "The App, including its design, code, and content, is the property of YourDocs Team " +
                "and is protected by applicable intellectual property laws. " +
                "You are granted a limited, non-exclusive, non-transferable license to use the App for personal purposes."
    )

    LegalHeading("9. Modifications to the Service")
    LegalParagraph(
        "We reserve the right to modify, suspend, or discontinue the App or any part thereof at any time, " +
                "with or without notice. " +
                "We shall not be liable to you or any third party for any modification, suspension, or discontinuation of the App."
    )

    LegalHeading("10. Changes to These Terms")
    LegalParagraph(
        "We may update these Terms from time to time. Changes will be communicated through App updates on Google Play. " +
                "Your continued use of the App after any changes constitutes acceptance of the updated Terms."
    )

    LegalHeading("11. Governing Law")
    LegalParagraph(
        "These Terms shall be governed by and construed in accordance with applicable laws, " +
                "without regard to conflict of law principles."
    )

    LegalHeading("12. Contact Us")
    LegalParagraph("If you have any questions about these Terms, please contact us at:")
    LegalContact()
}

@Composable
private fun LegalMeta(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun LegalHeading(text: String) {
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun LegalSubheading(text: String) {
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun LegalParagraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun LegalContact() {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("YourDocs Team")
            }
            append("\nEmail: yourdocsapp@gmail.com")
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f)
    )
}
