package com.desimeals.app.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Exporters {

    fun exportCsv(ctx: Context, fileName: String, header: List<String>, rows: List<List<String>>): File {
        val dir = File(ctx.filesDir, "exports").apply { mkdirs() }
        val f = File(dir, fileName)
        f.bufferedWriter().use { w ->
            w.appendLine(header.joinToString(",") { csvCell(it) })
            rows.forEach { r -> w.appendLine(r.joinToString(",") { csvCell(it) }) }
        }
        return f
    }

    private fun csvCell(s: String): String {
        val needs = s.contains(",") || s.contains("\"") || s.contains("\n")
        return if (needs) "\"" + s.replace("\"", "\"\"") + "\"" else s
    }

    /** Minimal dependency-free XLSX writer (single sheet). */
    fun exportXlsx(ctx: Context, fileName: String, sheet: String, header: List<String>, rows: List<List<String>>): File {
        val dir = File(ctx.filesDir, "exports").apply { mkdirs() }
        val f = File(dir, fileName)
        fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        fun rowXml(r: List<String>, rownum: Int): String {
            val cells = r.mapIndexed { i, v ->
                val col = ('A'.code + i).toChar()
                "<c r=\"$col$rownum\" t=\"inlineStr\"><is><t>${esc(v)}</t></is></c>"
            }.joinToString("")
            return "<row r=\"$rownum\">$cells</row>"
        }
        val allRows = StringBuilder()
        allRows.append(rowXml(header, 1))
        rows.forEachIndexed { i, r -> allRows.append(rowXml(r, i + 2)) }
        val sheetXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>$allRows</sheetData></worksheet>"""
        val wbXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="${esc(sheet)}" sheetId="1" r:id="rId1"/></sheets></workbook>"""
        val rels = """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
        val wbRels = """<?xml version="1.0" encoding="UTF-8"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>"""
        val contentTypes = """<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>"""
        ZipOutputStream(FileOutputStream(f)).use { z ->
            z.putNextEntry(ZipEntry("[Content_Types].xml")); z.write(contentTypes.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("_rels/.rels")); z.write(rels.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("xl/workbook.xml")); z.write(wbXml.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("xl/_rels/workbook.xml.rels")); z.write(wbRels.toByteArray()); z.closeEntry()
            z.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml")); z.write(sheetXml.toByteArray()); z.closeEntry()
        }
        return f
    }

    /** Simple multi-line PDF report using platform PdfDocument (no dependency). */
    fun exportPdf(
        ctx: Context,
        fileName: String,
        title: String,
        meta: List<String>,
        header: List<String>,
        rows: List<List<String>>,
        footerTotals: List<String> = emptyList()
    ): File {
        val dir = File(ctx.filesDir, "exports").apply { mkdirs() }
        val f = File(dir, fileName)
        val doc = PdfDocument()
        val pageW = 595; val pageH = 842
        val paint = Paint().apply { textSize = 11f }
        val paintBold = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val paintSmall = Paint().apply { textSize = 9f }
        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
        var y = 50f
        fun newPageIfNeeded(need: Float = 20f) {
            if (y + need > pageH - 50) {
                doc.finishPage(page); pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNum).create())
                y = 50f
            }
        }
        page.canvas.drawText(title, 40f, y, paintBold); y += 20
        meta.forEach { m -> newPageIfNeeded(); page.canvas.drawText(m.take(90), 40f, y, paintSmall); y += 14 }
        y += 6
        newPageIfNeeded(); page.canvas.drawText(header.joinToString(" | ").take(110), 40f, y, paintBold); y += 16
        page.canvas.drawLine(40f, y, (pageW - 40).toFloat(), y, paint); y += 14
        rows.forEach { r ->
            newPageIfNeeded()
            page.canvas.drawText(r.joinToString(" | ").take(110), 40f, y, paintSmall); y += 13
        }
        if (footerTotals.isNotEmpty()) {
            y += 8; newPageIfNeeded()
            page.canvas.drawLine(40f, y, (pageW - 40).toFloat(), y, paint); y += 16
            footerTotals.forEach { t -> newPageIfNeeded(); page.canvas.drawText(t.take(100), 40f, y, paintBold); y += 16 }
        }
        doc.finishPage(page)
        FileOutputStream(f).use { doc.writeTo(it) }
        doc.close()
        return f
    }
}
