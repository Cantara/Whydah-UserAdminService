#!/bin/bash
git remote add upstream https://github.com/altran/Whydah-UserAdminService.git
git fetch upstream
git merge upstream/master
git push
