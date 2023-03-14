from datetime import datetime
import pybgpstream
import os

stream = pybgpstream.BGPStream(
    # project="ris-live"
    project="routeviews-stream"
)

while True:
    try:
        current_time = datetime.now()
        file_path = "/usr/logs/basic_as/as_collect.{}.{}".format(
            current_time.strftime("%m"), current_time.strftime("%d"))
        day = current_time.strftime("%d")
        if not os.path.exists("/usr/logs/basic_as"):
            os.makedirs(file_path)
        try:
            asFile = open(file_path, "a")
            while day == current_time.strftime("%d") :
                current_time = datetime.now()
                as_path = None
                for elem in stream:
                    try:
                        elem_path = elem.fields['as-path']
                    except:
                        continue
                    if elem_path != as_path:
                        asFile.write(str(elem) + "\n")
                        as_path = elem_path
        finally:
            asFile.close()
            day = current_time.strftime("%d")
    except Exception as r:
        log_path = "/usr/logs/python_log"
        if not os.path.exists(log_path):
            os.makedirs(log_path)
        log_file = open(log_path + "/as_error.log", "a")
        log_file.write(datetime.now()+": "+r+"\n")
        log_file.close()
