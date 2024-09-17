package com.example.voicemedcareapp.medicalReports.SpeechToTextFragment

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.TextView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PDFGenerator(
    private val context: Context,
    private val textView: TextView,
    private val patientName: String?,
    private val patientAge: String?,
    private val patientCNP: String?,
    private val patientPhone: String?,
    private val patientEmail: String?,
    private val resultNER: String?
) {

    fun generatePDF(): File? {
        val textContent = textView.text.toString()
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val paint = Paint()
        page.canvas.drawText("Patient Report", 50f, 50f, paint)
        page.canvas.drawText("Name: $patientName", 50f, 80f, paint)
        page.canvas.drawText("Age: $patientAge", 50f, 110f, paint)
        page.canvas.drawText("CNP: $patientCNP", 50f, 140f, paint)
        page.canvas.drawText("Phone: $patientPhone", 50f, 170f, paint)
        page.canvas.drawText("Email: $patientEmail", 50f, 200f, paint)
        page.canvas.drawText("Report Content:", 50f, 270f, paint)  // Adjusted the y-position accordingly
        page.canvas.drawText(textContent, 50f, 300f, paint)
        pdfDocument.finishPage(page)

        val file = File(context.filesDir, "Patient_Report.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            Toast.makeText(context, "PDF generated and saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
        return null
    }
}