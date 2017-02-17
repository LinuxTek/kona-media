import com.linuxtek.kona.util.KFileUtil;
import com.linuxtek.kona.media.util.KImageUtil;

def filename = args[1];

data = KFileUtil.readBinaryFile(filename);

data = KImageUtil.getNormalizedImage(data);

KFileUtil.writeFile("normalized-" + filename, data);


