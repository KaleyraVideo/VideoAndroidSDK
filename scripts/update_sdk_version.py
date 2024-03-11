import sys
import re
import semver
import os
import fileinput

module_name = sys.argv[1]
current_version = sys.argv[2]
release_version = sys.argv[3]

def replace_regex(file, searchRegex, replaceExp):
    with open(file, "r+") as file:
        text = file.read()
        text = re.sub(searchRegex, replaceExp, text)
        file.seek(0, 0)  # seek to beginning
        file.write(text)
        file.truncate()  # get rid of any trailing characters

def replace(filename, current_version, new_version):
    with fileinput.FileInput(filename, inplace=True) as file:
        for line in file:
            print(line.replace(current_version, new_version), end='')

def new_version(current_version, release_version):
    ver = semver.Version.parse(current_version)
    if release_version == "patch" or release_version == "minor" or release_version == "major":
        new_version = ver.next_version(release_version)
    else:
        new_version = semver.Version.parse(release_version)
    return str(new_version)


def set_output(name, value):
    try:
        with open(os.environ['GITHUB_OUTPUT'], 'a') as fh:
            print(f'{name}={value}', file=fh)
    except:
        print("Could not set the github output")

semver_regex = r"(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\.(?:0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\+([0-9a-zA-Z-]+(?:\.[0-9a-zA-Z-]+)*))?"

try:
    new_version = new_version(current_version, release_version)
    # update
    print("Update Kaleyra Video SDK from ", current_version, " to ", new_version)
    if module_name != "video-common-ui" :
        replace_regex("../" + module_name + "/src/main/assets/kaleyra_video_wrapper_info.txt",semver_regex, new_version)
    replace("../publish.gradle", current_version, new_version)
    set_output("TAG", "v" + new_version)
except Exception as error:
    sys.exit("Did not update version" + error)
