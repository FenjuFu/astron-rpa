import pytest
import pytest_asyncio
from httpx import AsyncClient

from app.models.api_key import OpenAPIDB
from app.models.workflow import Workflow
from app.utils.api_key import APIKeyUtils

OWNER_ID = "owner-1001"
OTHER_ID = "other-2002"
PROJECT_ID = "access-test-project"


async def _add_api_key(db, user_id: str) -> str:
    """直接写入一条有效的 API Key，返回明文 key"""
    raw_key = APIKeyUtils.generate_api_key()
    db.add(
        OpenAPIDB(
            name=f"key-{user_id}",
            user_id=user_id,
            api_key=APIKeyUtils.hash_api_key(raw_key),
            prefix=raw_key[:8],
            is_active=1,
        )
    )
    await db.flush()
    return raw_key


@pytest_asyncio.fixture
async def owned_workflow(test_get_db):
    workflow = Workflow(project_id=PROJECT_ID, name="Owned Workflow", version=1, status=1, user_id=OWNER_ID)
    test_get_db.add(workflow)
    await test_get_db.flush()
    return workflow


@pytest.mark.asyncio
async def test_get_workflow_by_owner_session(client: AsyncClient, owned_workflow):
    """网关会话认证后设置的 user_id 为工作流所有者时可以获取"""
    response = await client.get(f"/workflows/get/{PROJECT_ID}", headers={"user_id": OWNER_ID})
    assert response.status_code == 200
    assert response.json()["data"]["workflow"]["project_id"] == PROJECT_ID


@pytest.mark.asyncio
async def test_get_workflow_by_other_user_session(client: AsyncClient, owned_workflow):
    """其他用户获取时与工作流不存在的返回相同"""
    response = await client.get(f"/workflows/get/{PROJECT_ID}", headers={"user_id": OTHER_ID})
    assert response.status_code == 200
    assert response.json()["data"] is None


@pytest.mark.asyncio
async def test_get_workflow_by_owner_api_key(client: AsyncClient, test_get_db, owned_workflow):
    raw_key = await _add_api_key(test_get_db, OWNER_ID)
    response = await client.get(f"/workflows/get/{PROJECT_ID}", headers={"Authorization": f"Bearer {raw_key}"})
    assert response.status_code == 200
    assert response.json()["data"]["workflow"]["project_id"] == PROJECT_ID


@pytest.mark.asyncio
async def test_get_workflow_by_other_user_api_key(client: AsyncClient, test_get_db, owned_workflow):
    raw_key = await _add_api_key(test_get_db, OTHER_ID)
    response = await client.get(f"/workflows/get/{PROJECT_ID}", headers={"Authorization": f"Bearer {raw_key}"})
    assert response.status_code == 200
    assert response.json()["data"] is None


@pytest.mark.asyncio
async def test_get_workflow_with_invalid_api_key(client: AsyncClient, owned_workflow):
    response = await client.get(f"/workflows/get/{PROJECT_ID}", headers={"Authorization": "Bearer not-a-real-key"})
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_get_workflow_without_credentials(client: AsyncClient, owned_workflow):
    response = await client.get(f"/workflows/get/{PROJECT_ID}")
    assert response.status_code == 401
