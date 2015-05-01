package org.snailya.exercise01

import io.backchat.hookup.{InboundMessage, OutboundMessage, TextMessage, WireFormat}

class TextProtocol extends WireFormat {
  override def name: String = "textProtocol"

  override def parseOutMessage(message: String): OutboundMessage = TextMessage(message)

  override def parseInMessage(message: String): InboundMessage = TextMessage(message)

  override def render(message: OutboundMessage): String = message match {
    case TextMessage(a) => a
  }

  override def supportsAck: Boolean = false
}
