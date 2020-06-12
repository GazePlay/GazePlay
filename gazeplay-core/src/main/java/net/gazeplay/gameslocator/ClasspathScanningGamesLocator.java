package net.gazeplay.gameslocator;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.gazeplay.GameSpecSource;

import java.util.ArrayList;
import java.util.List;

public class ClasspathScanningGamesLocator extends AbstractGamesLocator {

    @Override
    protected List<Class> findGameSpecSourceClasses() {
        final List<Class> result = new ArrayList<>();

        String pkg = "net.gazeplay.games";
        Class<GameSpecSource> searchedInterface = GameSpecSource.class;
        ClassGraph classGraph = new ClassGraph()
            .whitelistPackages(pkg) // Scan com.xyz and subpackages (omit to scan all packages)
            ;

        // Start the scan
        ScanResult scanResult = null;
        try {
            scanResult = classGraph.scan();
            for (ClassInfo routeClassInfo : scanResult.getClassesImplementing(searchedInterface.getName())) {
                Class<GameSpecSource> gameClass = routeClassInfo.loadClass(searchedInterface);
                result.add(gameClass);
            }
        } finally {
            if (scanResult != null) {
                scanResult.close();
            }
        }
        return result;
    }

}
