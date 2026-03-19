package com.maplestone.dataCollect.wal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maplestone.dataCollect.pojo.entity.DataPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * WAL (Write-Ahead Log) 管理器
 * 防止进程崩溃导致数据丢失
 */
@Slf4j
@Component
public class WalManager {

    @Value("${wal.enabled:true}")
    private boolean enabled;

    @Value("${wal.base-path:/data/wal}")
    private String basePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (enabled) {
            try {
                Files.createDirectories(Paths.get(basePath));
                log.info("WAL 初始化完成，路径: {}", basePath);
            } catch (IOException e) {
                log.error("WAL 初始化失败: {}", e.getMessage());
            }
        }
    }

    public String write(DataPoint dataPoint) {
        if (!enabled) {
            return null;
        }

        try {
            String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String dirPath = basePath + File.separator + date;
            Files.createDirectories(Paths.get(dirPath));

            String fileName = System.currentTimeMillis() + "_" + dataPoint.getStationId() + "_" + dataPoint.getPointId() + ".wal";
            Path filePath = Paths.get(dirPath, fileName);

            String json = objectMapper.writeValueAsString(dataPoint);
            Files.write(filePath, json.getBytes(), StandardOpenOption.CREATE);

            return filePath.toString();
        } catch (IOException e) {
            log.error("写入 WAL 失败: {}", e.getMessage());
            return null;
        }
    }

    public void delete(String walFilePath) {
        if (!enabled || walFilePath == null) {
            return;
        }

        try {
            Files.deleteIfExists(Paths.get(walFilePath));
        } catch (IOException e) {
            log.error("删除 WAL 文件失败: {}", e.getMessage());
        }
    }

    public List<DataPoint> recover() {
        List<DataPoint> dataPoints = new ArrayList<>();
        if (!enabled) {
            return dataPoints;
        }

        try {
            File baseDir = new File(basePath);
            if (!baseDir.exists()) {
                return dataPoints;
            }

            File[] dateDirs = baseDir.listFiles(File::isDirectory);
            if (dateDirs == null) {
                return dataPoints;
            }

            for (File dateDir : dateDirs) {
                File[] walFiles = dateDir.listFiles((dir, name) -> name.endsWith(".wal"));
                if (walFiles == null) {
                    continue;
                }

                for (File walFile : walFiles) {
                    try {
                        String json = new String(Files.readAllBytes(walFile.toPath()));
                        DataPoint dataPoint = objectMapper.readValue(json, DataPoint.class);
                        dataPoints.add(dataPoint);
                    } catch (IOException e) {
                        log.error("恢复 WAL 文件失败: {}, 错误: {}", walFile.getName(), e.getMessage());
                    }
                }
            }

            log.info("从 WAL 恢复数据: {} 条", dataPoints.size());
        } catch (Exception e) {
            log.error("WAL 恢复过程异常: {}", e.getMessage());
        }

        return dataPoints;
    }

    public void cleanup(int retentionHours) {
        if (!enabled) {
            return;
        }

        try {
            File baseDir = new File(basePath);
            if (!baseDir.exists()) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (retentionHours * 3600L * 1000L);
            File[] dateDirs = baseDir.listFiles(File::isDirectory);
            if (dateDirs == null) {
                return;
            }

            int deletedCount = 0;
            for (File dateDir : dateDirs) {
                if (dateDir.lastModified() < cutoffTime) {
                    deleteDirectory(dateDir);
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                log.info("清理过期 WAL 目录: {} 个", deletedCount);
            }
        } catch (Exception e) {
            log.error("WAL 清理失败: {}", e.getMessage());
        }
    }

    private void deleteDirectory(File directory) throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    Files.deleteIfExists(file.toPath());
                }
            }
        }
        Files.deleteIfExists(directory.toPath());
    }
}
