package com.example.myapplication.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayOutputStream

object PayslipPdfGenerator {

    fun generatePayslipPdf(
        empId: String,
        month: Int,
        year: Int,
        empName: String = "Rohan Das",
        details: com.example.myapplication.data.model.PayrollDetailsResponse? = null
    ): ResponseBody {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // --- Header ---
        paint.color = Color.parseColor("#3F51B5") // Brand Blue
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("OFFICIAL PAYSLIP", 50f, 60f, paint)

        // --- Body ---
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.isFakeBoldText = false

        var yPos = 150f
        canvas.drawText("Employee Name: $empName", 50f, yPos, paint); yPos += 30f
        canvas.drawText("Employee ID: $empId", 50f, yPos, paint); yPos += 30f
        canvas.drawText("Period: $month/$year", 50f, yPos, paint); yPos += 50f

        if (details != null && details.components != null) {
            paint.isFakeBoldText = true
            canvas.drawText("Earnings Breakdown:", 50f, yPos, paint); yPos += 30f
            paint.isFakeBoldText = false
            details.components.filter { it.compType == "EARNING" }.forEach {
                canvas.drawText("${it.compName}: ₹ ${String.format("%.2f", it.amount ?: 0.0)}", 70f, yPos, paint)
                yPos += 25f
            }
            yPos += 25f

            paint.isFakeBoldText = true
            canvas.drawText("Deductions:", 50f, yPos, paint); yPos += 30f
            paint.isFakeBoldText = false
            details.components.filter { it.compType == "DEDUCTION" }.forEach {
                canvas.drawText("${it.compName}: ₹ ${String.format("%.2f", it.amount ?: 0.0)}", 70f, yPos, paint)
                yPos += 25f
            }
            yPos += 25f

            paint.color = Color.parseColor("#3F51B5")
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Net Salary: ₹ ${String.format("%.2f", details.netSalary ?: 0.0)}", 50f, yPos, paint)
        } else {
            // Default Fallback content if no details provided
            paint.isFakeBoldText = true
            canvas.drawText("Earnings Breakdown:", 50f, yPos, paint); yPos += 30f
            paint.isFakeBoldText = false
            canvas.drawText("Basic Salary: ₹ 45,000.00", 70f, yPos, paint); yPos += 25f
            canvas.drawText("HRA: ₹ 18,000.00", 70f, yPos, paint); yPos += 25f
            canvas.drawText("Allowances: ₹ 7,500.00", 70f, yPos, paint); yPos += 50f

            paint.isFakeBoldText = true
            canvas.drawText("Deductions:", 50f, yPos, paint); yPos += 30f
            paint.isFakeBoldText = false
            canvas.drawText("PF: ₹ 5,400.00", 70f, yPos, paint); yPos += 25f
            canvas.drawText("Professional Tax: ₹ 200.00", 70f, yPos, paint); yPos += 50f

            paint.color = Color.parseColor("#3F51B5")
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Net Salary: ₹ 64,900.00", 50f, yPos, paint)
        }

        // --- Footer ---
        paint.color = Color.GRAY
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("This is a computer generated document.", 50f, 800f, paint)

        pdfDocument.finishPage(page)

        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()

        val pdfBytes = outputStream.toByteArray()
        return pdfBytes.toResponseBody("application/pdf".toMediaTypeOrNull())
    }
}
