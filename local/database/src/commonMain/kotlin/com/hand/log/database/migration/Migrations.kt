package com.hand.log.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

internal val MIGRATION_2_3 = object : Migration(2, 3) {
	override fun migrate(connection: SQLiteConnection) {
		connection.execSQL(
			"CREATE TABLE IF NOT EXISTS `quiz_records` (`id` TEXT NOT NULL, `quizType` TEXT NOT NULL, " +
				"`score` INTEGER NOT NULL, `total` INTEGER NOT NULL, `avgResponseMs` INTEGER NOT NULL, " +
				"`bestStreak` INTEGER NOT NULL, `playedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
		)
	}
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
	override fun migrate(connection: SQLiteConnection) {
		connection.execSQL("ALTER TABLE `quiz_records` ADD COLUMN `questions` TEXT NOT NULL DEFAULT '[]'")
	}
}

internal val MIGRATION_4_5 = object : Migration(4, 5) {
	override fun migrate(connection: SQLiteConnection) {
		connection.execSQL("ALTER TABLE `quiz_records` ADD COLUMN `stack` TEXT NOT NULL DEFAULT ''")
	}
}

internal val MIGRATION_5_6 = object : Migration(5, 6) {
	override fun migrate(connection: SQLiteConnection) {
		connection.execSQL(
			"CREATE TABLE IF NOT EXISTS `hand_records_new` (`id` TEXT NOT NULL, `tableId` TEXT, " +
				"`createdAt` INTEGER NOT NULL, `blinds` TEXT, `heroSeat` INTEGER NOT NULL, " +
				"`buttonSeat` INTEGER NOT NULL, `streets` TEXT NOT NULL, `players` TEXT NOT NULL, " +
				"`result` REAL, `resultLabel` TEXT, `memo` TEXT, `results` TEXT, PRIMARY KEY(`id`), " +
				"FOREIGN KEY(`tableId`) REFERENCES `poker_tables`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
		)
		connection.execSQL(
			"INSERT INTO `hand_records_new` (`id`, `tableId`, `createdAt`, `blinds`, `heroSeat`, " +
				"`buttonSeat`, `streets`, `players`, `result`, `resultLabel`, `memo`, `results`) " +
				"SELECT `id`, `tableId`, `createdAt`, `blinds`, `heroSeat`, `buttonSeat`, `streets`, " +
				"`players`, `result`, `resultLabel`, `memo`, `results` FROM `hand_records`",
		)
		connection.execSQL("DROP TABLE `hand_records`")
		connection.execSQL("ALTER TABLE `hand_records_new` RENAME TO `hand_records`")
		connection.execSQL(
			"CREATE INDEX IF NOT EXISTS `index_hand_records_tableId` ON `hand_records` (`tableId`)",
		)
	}
}
