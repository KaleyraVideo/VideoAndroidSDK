import sys
import re
import yaml

if len(sys.argv) <= 2:
    raise Exception("Missing arguments, call this script passing the release version and module name")

module_name = sys.argv[1]
new_version = sys.argv[2]

def replace(file, searchRegex, replaceExp):
  with open(file, "r+") as file:
      text = file.read()
      text = re.sub(searchRegex, replaceExp, text)
      file.seek(0, 0) # seek to beginning
      file.write(text)
      file.truncate() # get rid of any trailing characters

semver_regex = r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?"

try:
    # update
    print("-- Update Kaleyra Video Wrapper Info--")
    replace("../"+module_name+"/src/main/assets/kaleyra_video_wrapper_info.txt", semver_regex, new_version)
    print("Updated "+ module_name +"/"+new_version)
except:
    print("Did not update version for " + module_name)