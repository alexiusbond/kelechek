now="$(date +'%d_%m_%Y_%H_%M_%S')"
filename="db_backup_$now".gz
backupfolder="/home/star/backups"
fullpathbackupfile="$backupfolder/$filename"
mysqldump --user=root --password=AiU2017dB --default-character-set=utf8 lomonosov | gzip > "$fullpathbackupfile"
chmod 777 "$fullpathbackupfile"
exit 0
