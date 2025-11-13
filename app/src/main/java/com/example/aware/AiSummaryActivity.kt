package com.example.aware

import android.Manifest.permission_group.SMS
import android.R.attr.bottom
import android.R.attr.priority
import android.icu.text.ListFormatter
import android.icu.text.RelativeDateTimeFormatter
import android.icu.util.Output
import android.os.Bundle
import android.service.autofill.Validators.and
import android.system.Os.remove
import android.util.Log
import android.util.Log.e
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aware.database.NotificationDatabase
import com.example.aware.utils.NotificationRepository
import com.example.aware.utils.RecentNotificationRepository
import com.example.aware.utils.SummaryAdapter
import com.example.aware.utils.SummaryItem
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AiSummaryActivity : AppCompatActivity() {

    private lateinit var repo: NotificationRepository
    private lateinit var repo2: RecentNotificationRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_ai_summary)


        // Init repos same as Dashboard
        val dao = NotificationDatabase.getDatabase(this).notificationDao()
        repo = NotificationRepository(dao)

        val dao2 = NotificationDatabase.getDatabase(this).recentlyClearedNotificationDao()
        repo2 = RecentNotificationRepository(dao2)

        // Fetch on load
        fetchSummary()

        // Refresh button
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            fetchSummary()
        }


    }

    private fun fetchSummary() {

        lifecycleScope.launch(Dispatchers.IO) {

            val notifications = repo.dao.getNotificationsList()
            val cleared = repo2.dao.getRecentlyClearedList()

            val fullText = buildString {
                append("Unread:\n")
                notifications.forEach {
                    append("- ${it.appName}: ${it.notificationHeading} | ${it.notificationContent}\n")
                }
                append("\nCleared:\n")
                cleared.forEach {
                    append("- ${it.appName}: ${it.notificationHeading} | ${it.notificationContent}\n")
                }
            }

            val summary = summarizeWithGemini(fullText)

            withContext(Dispatchers.Main) {

                val items = parseSummary(summary)

                val recycler = findViewById<RecyclerView>(R.id.summaryRecycler)
                recycler.layoutManager = LinearLayoutManager(this@AiSummaryActivity)
                recycler.adapter = SummaryAdapter(items)
            }
        }
    }

}



    private fun parseSummary(summary: String): List<SummaryItem> {
        val items = mutableListOf<SummaryItem>()

        summary.lines().forEach { line ->
            val trimmed = line.trim()

            when {
                // Category detection: starts with ** and ends with **
                trimmed.startsWith("**") && trimmed.endsWith("**") -> {
                    val title = trimmed.replace("*", "")
                    items.add(SummaryItem.Category(title))
                }

                // Bullet detection
                trimmed.startsWith("•") -> {
                    items.add(SummaryItem.Entry(trimmed))
                }
            }
        }

        return items
    }


    private suspend fun summarizeWithGemini(text: String): String {
        return try {
            val client = GenerativeModel(
                modelName = "gemini-2.0-flash",
                apiKey = "AIzaSyCVb5E1hFveOZvh8-8jGTqg6yKeNQ6jOEo"
            )

            val prompt = """
            You are an intelligent notification assistant. Your job is to transform raw notifications into a clean, polished, human-friendly daily briefing. The goal is to produce something that feels like a modern AI summary, not a list of raw statements.

Write the summary in an engaging, helpful tone. Keep it natural, concise, and readable. Avoid repeating wording from the raw notifications unless necessary. Combine related notifications and infer reasonable context when obvious.

FORMAT RULES:
1. Group information under these section headers (bold):
   **1. Personal Messages**
   **2. Group Messages**
   **3. Important Alerts**
   **4. App Activity**
   **5. Promotions & Spam**

2. Under each section, provide short 1–2 line summaries, NOT literal copies of the notifications.
   Example:
   • Instead of: “Om shared a photo in the group.”
     Write: “Om shared a photo in the apartment group.”

   • Instead of: “Karan provided instructions.”
     Write: “Karan shared updated UMS upload guidelines.”

3. When multiple messages come from the same person or chat, merge them into ONE clean bullet point.

4. Make the wording feel smooth, like a modern assistant:
   - smart phrasing  
   - conversational but professional  
   - avoid stiff or repetitive structure  

5. DO NOT generate long essays. Keep everything crisp.

6. End with a short:
   **Overall Summary:** (1–2 lines highlighting the day’s themes)

7. Skip empty groups.

OUTPUT SHOULD LOOK LIKE:

==================================================
**1. Personal Messages**
• Few personal chats active; most messages are routine or quick replies.

**2. Group Messages**
• LPU groups are active with reminders for tomorrow’s project submissions and shared photos.
• Official SEM-7 group posted updated guidelines for uploading files on UMS.

**3. Important Alerts**
• Evening Focus Session scheduled for 6–8 PM.
• Hotspot is active with connected devices.

**4. App Activity**
• YouTube Music is currently playing.

**5. Promotions & Spam**
• Google Play Store sent a small limited-time discount.

**Overall Summary:** Mostly updates from LPU groups, a few reminders, and some regular system activity. Nothing urgent.
==================================================

NOW GENERATE THE DAILY BRIEFING FOR THE FOLLOWING RAW NOTIFICATIONS:

$text


        $text
    """.trimIndent()

            val result = client.generateContent(prompt)
            result.text ?: "No summary generated."
        } catch (e: Exception) {
            Log.e("AiSummaryActivity", "Error calling Gemini API", e)
            "Error summarizing notifications: ${e.localizedMessage}"
        }
    }
