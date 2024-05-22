package com.caixy.adminSystem.utils;

import lombok.Getter;
import lombok.ToString;

/**
 * 字节大小转化工具类
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.utils.SizeUtils
 * @since 2024-05-21 19:41
 **/
public class SizeUtils
{
    @Getter
    public enum SizeType
    {
        KB(1024L),
        MB(1024L * 1024L),
        GB(1024L * 1024L * 1024L),
        TB(1024L * 1024L * 1024L * 1024L);

        private final long bytes;

        SizeType(long bytes)
        {
            this.bytes = bytes;
        }

    }

    @ToString
    @Getter
    public static class ByteSize
    {
        private final long bytes;
        private final SizeType unit;

        public ByteSize(long bytes, SizeType unit)
        {
            this.bytes = bytes;
            this.unit = unit;
        }

        public boolean isLessThanOrEqualTo(long fileSize)
        {
            return this.bytes >= fileSize;
        }


        public long toBytes()
        {
            return bytes;
        }

        public double toKB()
        {
            return bytes / (double) SizeType.KB.getBytes();
        }

        public double toMB()
        {
            return bytes / (double) SizeType.MB.getBytes();
        }

        public double toGB()
        {
            return bytes / (double) SizeType.GB.getBytes();
        }

        public double toTB()
        {
            return bytes / (double) SizeType.TB.getBytes();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            ByteSize byteSize = (ByteSize) obj;
            return bytes == byteSize.bytes;
        }

        @Override
        public int hashCode()
        {
            return Long.hashCode(bytes);
        }
    }

    /**
     * 将字节数转换为指定单位的大小
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/5/21 下午7:53
     */
    public static ByteSize of(long value, SizeType unit)
    {
        if (value > Long.MAX_VALUE / unit.getBytes())
        {
            throw new ArithmeticException("Overflow: multiplying " + value + " by " + unit.getBytes() + " would exceed Long.MAX_VALUE");
        }
        return new ByteSize(value * unit.getBytes(), unit);
    }
}
