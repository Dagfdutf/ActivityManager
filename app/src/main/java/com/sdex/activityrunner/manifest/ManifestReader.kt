package com.sdex.activityrunner.manifest

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.support.annotation.WorkerThread
import android.text.TextUtils
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringWriter
import java.nio.charset.Charset
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class ManifestReader {

  @WorkerThread
  fun loadAndroidManifest(context: Context, packageName: String): String? {
    try {
      return formatManifest(load(context, packageName))
    } catch (e: TransformerException) {
      e.printStackTrace()
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    } catch (e: XmlPullParserException) {
      e.printStackTrace()
    }

    return null
  }

  @Throws(PackageManager.NameNotFoundException::class, IOException::class, XmlPullParserException::class)
  private fun load(context: Context, packageName: String): String {
    val packageManager = context.packageManager
    val resources = packageManager.getResourcesForApplication(packageName)
    val parser = resources.assets.openXmlResourceParser("AndroidManifest.xml")
    val stringBuilder = StringBuilder()
    var eventType = parser.next()

    while (eventType != XmlResourceParser.END_DOCUMENT) {
      // start tag found
      if (eventType == XmlResourceParser.START_TAG) {
        //start with opening element and writing its name
        stringBuilder.append("<").append(parser.name)

        //for each attribute in given element append attrName="attrValue"
        for (i in 0 until parser.attributeCount) {
          val attributeName = parser.getAttributeName(i)
          val attributeValue = getAttributeValue(attributeName,
            parser.getAttributeValue(i), resources)
          stringBuilder.append(" ").append(attributeName)
            .append("=\"").append(attributeValue).append("\"")
        }

        stringBuilder.append(">")
        if (parser.text != null) {
          // if there is  body of xml element, add it there
          stringBuilder.append(parser.text)
        }
      } else if (eventType == XmlResourceParser.END_TAG) {
        stringBuilder.append("</").append(parser.name).append(">")
      }
      eventType = parser.next()
    }
    return stringBuilder.toString()
  }

  private fun getAttributeValue(attributeName: String, attributeValue: String,
                                resources: Resources): String {
    if (attributeValue.startsWith("@")) {
      try {
        val id = Integer.valueOf(attributeValue.substring(1))
        val value = if (attributeName == "theme" || attributeName == "resource") {
          resources.getResourceEntryName(id)
        } else {
          resources.getString(id)
        }
        return TextUtils.htmlEncode(value)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return attributeValue
  }

  @Throws(TransformerException::class)
  private fun formatManifest(data: String): String {
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
    val result = StreamResult(StringWriter())
    val dataBytes = data.toByteArray(Charset.forName("UTF-8"))
    val source = StreamSource(ByteArrayInputStream(dataBytes))
    transformer.transform(source, result)
    return result.writer.toString()
  }
}