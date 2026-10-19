import json
import os
import tempfile
import unittest
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch
from urllib.parse import quote

import pytest
from astronverse.enterprise.enterprise import Enterprise, atomicMg
from astronverse.enterprise.error import BaseException
from requests.structures import CaseInsensitiveDict


class TestEnterprise(unittest.TestCase):
    """测试Enterprise共享文件夹上传/下载，HTTP请求均为mock"""

    def setUp(self):
        tmp_dir = tempfile.TemporaryDirectory()
        self.addCleanup(tmp_dir.cleanup)
        self.tmp_dir = tmp_dir.name

        cfg_patcher = patch.object(atomicMg, "cfg", return_value={"GATEWAY_PORT": "8888"})
        cfg_patcher.start()
        self.addCleanup(cfg_patcher.stop)

    def _write_file(self, name, content=b"hello"):
        path = os.path.join(self.tmp_dir, name)
        Path(path).write_bytes(content)
        return path

    @staticmethod
    def _download_response(filename, content):
        # resource-service FileServiceImpl#downloadFile 的响应头格式
        return SimpleNamespace(
            status_code=200,
            headers=CaseInsensitiveDict(
                {
                    "Content-Type": "application/octet-stream",
                    "Content-Disposition": "attachment;filename=" + quote(filename),
                }
            ),
            iter_content=lambda chunk_size: [content],
        )

    def test_upload_uses_share_file_upload_and_passes_file_id(self):
        file_path = self._write_file("test.txt")
        # resource-service FileController#shareFileUpload 返回 ShareFileUploadVo(fileId, type, fileName)
        upload_response = SimpleNamespace(
            status_code=200,
            text=json.dumps(
                {
                    "code": "000000",
                    "data": {"fileId": "file-123", "type": 3, "fileName": "test.txt"},
                }
            ),
        )
        info_response = SimpleNamespace(status_code=200, text="", json=lambda: {"code": "000000"})

        with patch(
            "astronverse.enterprise.enterprise.requests.post",
            side_effect=[upload_response, info_response],
        ) as mock_post:
            result = Enterprise.upload_to_sharefolder(file_path=file_path)

        assert result == "上传成功"
        assert mock_post.call_count == 2

        upload_call, info_call = mock_post.call_args_list
        assert upload_call.args[0] == "http://127.0.0.1:8888/api/resource/file/share-file-upload"
        assert info_call.args[0] == "http://127.0.0.1:8888/api/robot/robot-shared-file/addSharedFileInfo"
        assert info_call.kwargs["json"] == {"fileId": "file-123", "fileType": 3, "fileName": "test.txt", "tags": []}

    def test_upload_rejects_missing_file(self):
        with (
            patch("astronverse.enterprise.enterprise.requests.post") as mock_post,
            pytest.raises(BaseException),
        ):
            Enterprise.upload_to_sharefolder(file_path=os.path.join(self.tmp_dir, "missing.txt"))

        mock_post.assert_not_called()

    def test_download_saves_file_with_decoded_name(self):
        with patch(
            "astronverse.enterprise.enterprise.requests.get",
            return_value=self._download_response("测试.txt", b"content"),
        ) as mock_get:
            save_path = Enterprise.download_from_sharefolder(file_path=123, save_folder=self.tmp_dir)

        assert mock_get.call_args.args[0] == "http://127.0.0.1:8888/api/resource/file/download"
        assert mock_get.call_args.kwargs["params"] == {"fileId": 123}
        assert save_path == os.path.join(self.tmp_dir, "测试.txt")
        assert Path(save_path).read_bytes() == b"content"

    def test_download_renames_when_file_exists(self):
        self._write_file("测试.txt", b"old")

        with patch(
            "astronverse.enterprise.enterprise.requests.get",
            return_value=self._download_response("测试.txt", b"new"),
        ):
            save_path = Enterprise.download_from_sharefolder(file_path=123, save_folder=self.tmp_dir)

        assert save_path == os.path.join(self.tmp_dir, "测试(1).txt")
        assert Path(self.tmp_dir, "测试.txt").read_bytes() == b"old"

    def test_download_raises_on_json_error_response(self):
        error_response = SimpleNamespace(
            status_code=200,
            headers=CaseInsensitiveDict({"Content-Type": "application/json"}),
            json=lambda: {"code": "500000", "message": "文件在S3中不存在"},
        )

        with (
            patch("astronverse.enterprise.enterprise.requests.get", return_value=error_response),
            pytest.raises(BaseException),
        ):
            Enterprise.download_from_sharefolder(file_path=123, save_folder=self.tmp_dir)

    def test_download_rejects_relative_folder(self):
        with (
            patch("astronverse.enterprise.enterprise.requests.get") as mock_get,
            pytest.raises(Exception, match="不是绝对路径"),
        ):
            Enterprise.download_from_sharefolder(file_path=123, save_folder="relative/folder")

        mock_get.assert_not_called()


if __name__ == "__main__":
    unittest.main()
