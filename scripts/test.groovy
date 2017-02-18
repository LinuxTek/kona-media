import com.linuxtek.kona.util.KFileUtil;
import com.linuxtek.kona.media.util.KImageUtil;
import com.linuxtek.kona.media.model.KImage;

def filename = args[1];

data = KFileUtil.readBinaryFile(filename);

image = KImageUtil.getNormalizedImage(data);

KFileUtil.writeFile("normalized-" + filename, image.data);


