package com.yourdocs.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.room.Room
import com.yourdocs.MainActivity
import com.yourdocs.data.local.database.YourDocsDatabase
import com.yourdocs.data.local.entity.toDomain

class YourDocsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = Room.databaseBuilder(
            context,
            YourDocsDatabase::class.java,
            YourDocsDatabase.DATABASE_NAME
        )
            .addMigrations(
                YourDocsDatabase.MIGRATION_1_2,
                YourDocsDatabase.MIGRATION_2_3,
                YourDocsDatabase.MIGRATION_3_4
            )
            .build()

        val folders = try {
            db.folderDao().getAllFoldersWithCount()
                .sortedByDescending { it.isPinned }
                .take(5)
                .map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "YourDocs",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        modifier = GlanceModifier.padding(bottom = 8.dp)
                    )

                    if (folders.isEmpty()) {
                        Text(
                            text = "No folders yet",
                            style = TextStyle(fontSize = 13.sp)
                        )
                    } else {
                        LazyColumn {
                            items(folders) { folder ->
                                Row(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(actionStartActivity<MainActivity>()),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = folder.emoji ?: "\uD83D\uDCC1",
                                        modifier = GlanceModifier.padding(end = 8.dp)
                                    )
                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        Text(
                                            text = folder.name,
                                            style = TextStyle(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                        )
                                        Text(
                                            text = "${folder.documentCount} docs",
                                            style = TextStyle(
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
