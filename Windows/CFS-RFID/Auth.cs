namespace CFS_RFID
{
    public class Auth
    {
        public byte Version { get; } = 0x01;
        public byte Msb { get; set; }
        public byte Lsb { get; set; }
        public byte KeyType { get; set; }
        public byte KeyNumber { get; set; }

        public byte[] ToArray()
        {
            return new[] { Version, Msb, Lsb, KeyType, KeyNumber };
        }
    }
}
