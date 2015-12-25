package scorex.account

import scorex.crypto.CryptographicHashImpl
import scorex.crypto.encode.Base58


class Account(val address: String) extends Serializable {

  lazy val bytes = Base58.decode(address).get

  override def toString = address

  override def equals(b: Any) = b match {
    case a: Account => a.address == address
    case _ => false
  }

  override def hashCode(): Int = address.hashCode()
}


object Account {

  import CryptographicHashImpl._


  val AddressVersion: Byte = 1
  val ChecksumLength = 4
  val HashLength = 20
  val AddressLength = 1 + ChecksumLength + HashLength

  def fromPubkey(publicKey: Array[Byte]): String = {
    val publicKeyHash = hash(publicKey).take(HashLength)
    val withoutChecksum = AddressVersion +: publicKeyHash //prepend ADDRESS_VERSION
    val checkSum = hash(withoutChecksum).take(ChecksumLength)

    Base58.encode(withoutChecksum ++ checkSum)
  }

  def isValidAddress(address: String): Boolean =
    Base58.decode(address).map { addressBytes =>
      if (addressBytes.length != Account.AddressLength)
        false
      else {
        val checkSum = addressBytes.takeRight(ChecksumLength)

        val dh = CryptographicHashImpl.hash(addressBytes.dropRight(ChecksumLength))
        val checkSumGenerated = dh.take(ChecksumLength)

        checkSum.sameElements(checkSumGenerated)
      }
    }.getOrElse(false)
}