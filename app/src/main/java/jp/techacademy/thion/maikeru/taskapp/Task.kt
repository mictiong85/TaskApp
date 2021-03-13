package jp.techacademy.thion.maikeru.taskapp

import io.realm.RealmObject
import java.io.Serializable
import java.util.Date
import io.realm.annotations.PrimaryKey

open class Task : RealmObject(), Serializable {
    var title: String = ""      // タイトル
    var contents: String = ""   // 内容
    var category: String = ""     // Category
    var date: Date = Date()     // 日時


    // idをプライマリーキーとして設定

    @PrimaryKey
    var id: Int = 0
}