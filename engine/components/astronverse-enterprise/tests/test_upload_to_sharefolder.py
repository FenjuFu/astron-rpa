import json
import os
import tempfile
import unittest
from types import SimpleNamespace
from unittest.mock import patch

from astronverse.enterprise.enterprise import Enterprise, atomicMg


class TestUploadToSharefolder(unittest.TestCase):
    """测试Enterprise.upload_to_sharefolder与服务端接口契约一致"""

    def setUp(self):
        fd, self.file_path = tempfile.mkstemp(suffix=".txt")
        with os.fdopen(fd, "wb") as f:
            f.write(b"hello")

    def tearDown(self):
        os.remove(self.file_path)

    def test_upload_uses_share_file_upload_and_passes_file_id(self):
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

        with (
            patch.object(atomicMg, "cfg", return_value={"GATEWAY_PORT": "8888"}),
            patch(
                "astronverse.enterprise.enterprise.requests.post",
                side_effect=[upload_response, info_response],
            ) as mock_post,
        ):
            result = Enterprise.upload_to_sharefolder(file_path=self.file_path)

        assert result == "上传成功"
        assert mock_post.call_count == 2

        upload_call, info_call = mock_post.call_args_list
        assert upload_call.args[0] == "http://127.0.0.1:8888/api/resource/file/share-file-upload"
        assert info_call.args[0] == "http://127.0.0.1:8888/api/robot/robot-shared-file/addSharedFileInfo"
        assert info_call.kwargs["json"] == {"fileId": "file-123", "fileType": 3, "fileName": "test.txt", "tags": []}


if __name__ == "__main__":
    unittest.main()
