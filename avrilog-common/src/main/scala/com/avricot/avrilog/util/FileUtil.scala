package com.avricot.avrilog.util

import java.io.FileInputStream

object FileUtil {
  val classPathConfig = "classpath:"

  /**
   * Return the inputStream, search in the classpath if the path starts with "classpath:".
   */
  def getInputStream(filePath: String) = {
    filePath match {
      case path if path.startsWith("classpath:") => this.getClass().getClassLoader().getResourceAsStream(filePath.substring(classPathConfig.size))
      case _ => new FileInputStream(filePath)
    }
  }
}