package com.timepath.steam.io;

import com.timepath.steam.io.storage.ACF;
import com.timepath.vfs.SimpleVFile;
import com.timepath.vfs.VFile;

import java.io.*;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author TimePath
 */
class DiffGen {

    private static final Logger   LOG       = Logger.getLogger(DiffGen.class.getName());
    private static final String[] blacklist = {
            ".*/bin",
            ".*/cache",
            ".*tf/custom",
            ".*tf/download",
            ".*tf/replay",
            ".*tf/screenshots",
            ".*sounds?\\.cache",
            ".*cfg/config\\.cfg",
            ".*media/viewed\\.res",
            ".*tf/console\\.log",
            ".*tf/condump.*\\.txt",
            ".*tf/demoheader\\.tmp",
            ".*tf/voice_ban\\.dt",
            ".*tf/trainingprogress\\.txt"
    };
    private static final int      K         = 1024, M = K * K, G = M * K;

    private DiffGen() {}

    private static boolean check(File f) {
        String path = f.getPath();
        for(String r : blacklist) {
            if(path.matches(r)) {
                LOG.info(path);
                return false;
            }
        }
        return true;
    }

    private static void extract(VFile<? extends VFile> v, File dir) throws IOException {
        File out = new File(dir, v.getName());
        if(!check(out)) {
            return;
        }
        if(v.isDirectory()) {
            out.mkdir();
            for(VFile e : v.list()) {
                extract(e, out);
            }
        } else {
            out.createNewFile();
            InputStream is = v.stream();
            FileOutputStream fos = new FileOutputStream(out);
            BufferedOutputStream os = new BufferedOutputStream(fos);
            byte[] buf = new byte[8 * M]; // r/w buffer
            int read;
            while(( read = is.read(buf) ) >= 0) {
                os.write(buf, 0, read);
            }
            os.flush();
            os.close();
        }
    }

    public static void main(String... args) throws IOException {
        int[] apps = { 440 };
        File container = new File(System.getProperty("user.home"), "steamtracker");
        for(int i : apps) {
            File repo = new File(container, String.valueOf(i));
            repo.mkdirs();
            ACF acf = ACF.fromManifest(i);
            Collection<? extends SimpleVFile> files = acf.list();
            for(SimpleVFile v : files) {
                extract(v, repo);
            }
        }
        LOG.info("EXTRACTED");
    }
}
